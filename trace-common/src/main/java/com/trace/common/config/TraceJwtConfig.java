package com.trace.common.config;

import com.trace.common.util.JwtUtil;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 密钥装配
 * <p>
 * JwtUtil 使用静态 SECRET，便于工具类静态方法调用；这里在容器启动时把配置值注入进去。
 */
@Configuration
public class TraceJwtConfig {

    @Value("${trace.jwt.secret:agriTraceCarbonFootprintNotaryDefaultSecretKey2026}")
    private String secret;

    @PostConstruct
    public void init() {
        if (secret != null && !secret.trim().isEmpty()) {
            JwtUtil.SECRET = secret.trim();
        }
    }
}
