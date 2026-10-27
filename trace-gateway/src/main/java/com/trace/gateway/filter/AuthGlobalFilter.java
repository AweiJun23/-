package com.trace.gateway.filter;

import com.trace.common.constant.Constants;
import com.trace.common.util.JwtUtil;
import com.trace.gateway.config.GatewayProperties;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关全局鉴权过滤器
 * <p>
 * 职责：
 * 1. 白名单路径直接放行（登录、注册、消费者扫码溯源、静态文件）；
 * 2. 校验 JWT 有效性，过期 / 非法 Token 返回 401；
 * 3. 校验通过后清除 Authorization 头，改写 X-User-* 内部头透传给下游微服务，
 *    避免 JWT 在内部网络重复传播，也让下游微服务天然信任该身份。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthGlobalFilter(GatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (matchWhitelist(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        Claims claims = JwtUtil.parseToken(token);
        if (claims == null || JwtUtil.isExpired(claims)) {
            return unauthorized(exchange, "未登录或登录已过期");
        }

        String userId = String.valueOf(claims.get("userId"));
        String role = claims.get("role", String.class);
        String channel = claims.get("channel", String.class);
        String username = claims.get("username", String.class);

        ServerHttpRequest mutated = request.mutate()
                .headers(headers -> {
                    headers.remove(Constants.AUTH_HEADER);
                    headers.set(Constants.USER_ID_HEADER, userId);
                    headers.set(Constants.USER_ROLE_HEADER, role == null ? "" : role);
                    headers.set("X-User-Channel", channel == null ? "" : channel);
                    headers.set("X-User-Name", username == null ? "" : username);
                })
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean matchWhitelist(String path) {
        List<String> whitelist = properties.getWhitelist();
        if (whitelist == null) {
            return false;
        }
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        // 放行 GET 类型的溯源公开接口（消费者扫码无需登录）
        return false;
    }

    private String resolveToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(Constants.AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(Constants.TOKEN_PREFIX)) {
            return header.substring(Constants.TOKEN_PREFIX.length());
        }
        return header;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        String body = "{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
