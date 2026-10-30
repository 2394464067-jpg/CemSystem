package com.ppphui.cemsystem.security;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // 1. 取 Authorization 请求头
        String header = request.getHeader("Authorization");

        // 2. 没有 token → 直接放行（让 Security 的授权规则去判定返回 401）
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. 截掉 "Bearer " 拿到纯 token
        String token = header.substring(7);

        try {
            // 4. 解析 token
            Claims claims = jwtUtil.parseToken(token);

            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            String factoryName = claims.get("factoryname", String.class);

            LoginUser loginUser = new LoginUser(userId.intValue(), username, role,factoryName);

            // 5. 存到自己的 ThreadLocal（供 Controller 使用）
            LoginContext.set(loginUser);

            // 6. ★ 关键：塞进 SecurityContextHolder，告诉 Security "该用户已认证"
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            loginUser,                                      // principal
                            null,                                           // credentials
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))  // 权限
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // token 无效或过期 —— 不设置认证信息，Security 后续会返回 401
            // 这里不打日志也行，因为 Security 会统一处理
        }

        // 7. 继续过滤链
        chain.doFilter(request, response);
    }

    @Override
    protected void doFilterNestedErrorDispatch(HttpServletRequest request,
                                               HttpServletResponse response,
                                               FilterChain filterChain) {
        // 保持默认即可
    }
}