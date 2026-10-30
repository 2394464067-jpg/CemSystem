package com.ppphui.cemsystem.log.aspect;

import com.ppphui.cemsystem.log.Log;
import com.ppphui.cemsystem.pojo.SysLog;
import com.ppphui.cemsystem.security.LoginUser;
import com.ppphui.cemsystem.service.SysLogService;
import com.ppphui.cemsystem.security.LoginContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class WebLogAspect {

    private final SysLogService sysLogService;

    /** 复用 Spring 容器里的 ObjectMapper，避免每次都 new */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 记录开始时间，防止并发串号，用 ThreadLocal */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    /** 切入点：拦截所有标注了 @Log 的方法 */
    @Pointcut("@annotation(com.ppphui.cemsystem.log.Log)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        START_TIME.set(System.currentTimeMillis());

        SysLog sysLog = new SysLog();
        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            throwable = e;
            throw e; // 必须抛出，否则全局异常处理器捕获不到
        } finally {
            try {
                buildAndSaveLog(joinPoint, throwable);
            } catch (Exception e) {
                // 切面自身异常不能影响业务
                log.error("记录操作日志异常", e);
            } finally {
                START_TIME.remove();
            }
        }
    }

    private void buildAndSaveLog(ProceedingJoinPoint joinPoint, Throwable throwable) {
        // 1. 基本信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Log logAnno = signature.getMethod().getAnnotation(Log.class);

        SysLog sysLog = new SysLog();
        sysLog.setModule(logAnno != null ? logAnno.module() : "");
        sysLog.setType(logAnno != null ? logAnno.type() : "");

        LoginUser loginUser = LoginContext.get();
        sysLog.setOperator(loginUser != null ? loginUser.getUserName() : "anonymous");

        sysLog.setCostTime(System.currentTimeMillis() - START_TIME.get());
        sysLog.setStatus(throwable == null ? 1 : 0);
        sysLog.setErrorMsg(throwable == null ? null : truncate(throwable.getMessage(), 500));
        sysLog.setCreateTime(LocalDateTime.now());
        sysLog.setUpdateTime(LocalDateTime.now());

        // 2. 请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            sysLog.setRequestMethod(request.getMethod());
            sysLog.setRequestUrl(request.getRequestURI());
            sysLog.setIp(getClientIp(request));
        }

        // 3. 参数序列化（跳过无法序列化的类型）
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            String params = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof MultipartFile)
                            && !(arg instanceof HttpServletRequest)
                            && !(arg instanceof jakarta.servlet.http.HttpServletResponse))
                    .map(this::toJsonSafe)
                    .collect(Collectors.joining(", "));
            sysLog.setRequestParams(truncate(params, 2000));
        }

        // 4. 从参数中提取 targetId（约定：方法参数名或类型为 Long 且名含 id）
        sysLog.setTargetId(extractTargetId(signature.getParameterNames(), args));

        // 5. 异步落库
        sysLogService.saveAsync(sysLog);

        // 6. 控制台打印一份，开发期方便排查
        log.info("操作日志 -> module={}, type={}, operator={}, url={}, cost={}ms, status={}",
                sysLog.getModule(), sysLog.getType(), sysLog.getOperator(),
                sysLog.getRequestUrl(), sysLog.getCostTime(), sysLog.getStatus());
    }

    /** 安全 JSON 序列化 */
    private String toJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    /** 从参数中提取目标数据 ID（约定参数名以 id 结尾，或类型为 Long 且名含 Id） */
    private Long extractTargetId(String[] paramNames, Object[] args) {
        if (paramNames == null || args == null) {
            return null;
        }
        for (int i = 0; i < paramNames.length; i++) {
            String name = paramNames[i];
            if (name != null
                    && name.toLowerCase().endsWith("id")
                    && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        return null;
    }

    /** 获取客户端真实 IP（兼容 Nginx 代理） */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能是多个 IP 逗号分隔，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /** 字符串截断，防止超长字段插入报错 */
    private String truncate(String str, int maxLen) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
