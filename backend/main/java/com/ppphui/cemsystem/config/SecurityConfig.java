package com.ppphui.cemsystem.config;

import com.ppphui.cemsystem.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig{

    private final JwtAuthFilter jwtAuthFilter;

    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("log-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/doc.html",
                "/webjars/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/knife4j/**"
        );
    }

    /** ============ 密码加密器 ============ */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** ============ CORS 配置 ============ */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的前端来源（生产环境换成具体域名）
        config.setAllowedOriginPatterns(List.of("*"));

        // 允许的请求方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头（Authorization 必须包含）
        config.setAllowedHeaders(List.of("*"));

        // 允许携带凭证（Authorization 头）
        config.setAllowCredentials(true);

        // 暴露给前端的响应头（如果前端需要读自定义头，比如下载文件名）
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** ============ Security 过滤链 ============ */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 启用 CORS，使用上面那个 Bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 关闭 CSRF（JWT 无状态，不需要）
                .csrf(AbstractHttpConfigurer::disable)

                // 3. 关闭默认表单登录和 HTTP Basic
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 4. 无状态：不创建 Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. 请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 白名单：登录、注册、错误页
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/error"
                        ).permitAll()

                        // 后续可按角色控制（示例，现在可以不加）

                        // .requestMatchers("/api/carbon/**").hasRole("USER")

                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )

                // 6. ★ 把 JwtAuthFilter 挂到 Security 过滤器链
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
