package com.ppphui.cemsystem.service;

import com.ppphui.cemsystem.pojo.SysLog;
import com.ppphui.cemsystem.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogService{

    private final SysLogMapper sysLogMapper;

    /**
     * 异步写入日志表
     * 注意：@Async 方法必须通过 Spring 代理调用才生效，
     * 因此切面中注入的是 SysLogService，而不是直接 new
     */

    @Async("logExecutor")
    public void saveAsync(SysLog sysLog) {
        try {
            sysLogMapper.insert(sysLog);
        } catch (Exception e) {
            // 日志落库失败不能影响业务，只打印
            log.error("操作日志落库失败: {}", sysLog, e);
        }
    }
}