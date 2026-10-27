package com.trace.gateway.config;

import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * 网关 WebFlux 配置
 * <p>
 * 跨域规则统一在 application.yml 的 spring.cloud.gateway.globalcors 中声明，
 * 这里仅集中管理超时常量，供路由元数据引用。
 */
@Configuration
public class GatewayCorsConfig implements WebFluxConfigurer {

    /** 网关连接超时：10 秒 */
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    /** 网关响应超时：30 秒 */
    public static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(30);
}
