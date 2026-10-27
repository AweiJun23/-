package com.trace.gateway.filter;

import com.trace.gateway.config.GatewayProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关接口限流过滤器（内存令牌桶）
 * <p>
 * 设计取舍：农业场景并发量有限，为避免网关强依赖 Redis 可用性，这里采用进程内令牌桶；
 * 若生产环境需要分布式限流，可将 {@link #buckets} 替换为 RedisRateLimiter + KeyResolver 即可。
 * <p>
 * 统一对 APP / 小程序 / Web 后台三类入口的请求做限流，防止恶意刷接口。
 */
@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitGlobalFilter.class);

    private final int replenishRate;
    private final int burstCapacity;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitGlobalFilter(GatewayProperties properties) {
        this.replenishRate = properties.getRateLimit().getReplenishRate();
        this.burstCapacity = properties.getRateLimit().getBurstCapacity();
        log.info("网关限流已启用： replenishRate={}/s, burstCapacity={}", replenishRate, burstCapacity);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = clientIp(exchange.getRequest());
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(burstCapacity));
        if (bucket.tryConsume(replenishRate, burstCapacity)) {
            return chain.filter(exchange);
        }
        log.warn("触发限流 ip={} uri={}", key, exchange.getRequest().getURI().getPath());
        return tooManyRequests(exchange.getResponse());
    }

    private String clientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress() == null
                    ? "unknown"
                    : request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    private Mono<Void> tooManyRequests(ServerHttpResponse httpResponse) {
        httpResponse.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        httpResponse.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        String body = "{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\",\"data\":null}";
        DataBuffer buffer = httpResponse.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return httpResponse.writeWith(Mono.just(buffer));
    }

    /**
     * 简易令牌桶
     */
    private static class Bucket {
        private final AtomicLong tokens;
        private volatile long lastRefillMillis = System.currentTimeMillis();

        Bucket(long capacity) {
            this.tokens = new AtomicLong(capacity);
        }

        synchronized boolean tryConsume(int replenishRate, int burstCapacity) {
            long now = System.currentTimeMillis();
            long elapsedMillis = now - lastRefillMillis;
            if (elapsedMillis > 0) {
                long refill = elapsedMillis * replenishRate / Duration.ofSeconds(1).toMillis();
                if (refill > 0) {
                    long updated = Math.min(burstCapacity, tokens.get() + refill);
                    tokens.set(updated);
                    lastRefillMillis = now;
                }
            }
            if (tokens.get() <= 0) {
                return false;
            }
            tokens.decrementAndGet();
            return true;
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
