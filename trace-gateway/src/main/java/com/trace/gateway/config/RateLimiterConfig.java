package com.trace.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * 网关装配配置
 * <p>
 * 说明：限流采用 {@link com.trace.gateway.filter.RateLimitGlobalFilter} 的内存令牌桶实现，
 * 因此此处无需额外装配 Bean。若后续要切换为分布式限流，可在此处注册
 * RedisRateLimiter 与 KeyResolver，并把过滤器中的 {@code buckets} 替换为 Redis 实现。
 */
@Configuration
public class RateLimiterConfig {
}
