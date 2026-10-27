package com.trace.common.web;

import com.trace.common.constant.Constants;
import com.trace.common.context.UserContext;
import com.trace.common.domain.LoginUser;
import com.trace.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 微服务侧鉴权拦截器
 * <p>
 * 双通道获取身份：
 * 1) 内部调用：网关校验 JWT 后透传 X-User-Id / X-User-Role 请求头；
 * 2) 直连场景（如本地调试、MQ 回调）：自行解析 Authorization 中的 JWT。
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final Log log = LogFactory.getLog(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先读取网关透传的身份
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        String role = request.getHeader(Constants.USER_ROLE_HEADER);
        String channel = request.getHeader("X-User-Channel");
        String username = request.getHeader("X-User-Name");

        LoginUser loginUser = null;
        if (userId != null && !userId.isEmpty()) {
            try {
                loginUser = new LoginUser(Long.parseLong(userId), username, role, channel, null);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }

        if (loginUser == null) {
            // 降级：自行解析 JWT
            String token = resolveToken(request);
            Claims claims = JwtUtil.parseToken(token);
            if (claims != null && !JwtUtil.isExpired(claims)) {
                loginUser = JwtUtil.toLoginUser(claims);
            }
        }

        if (loginUser != null) {
            UserContext.set(loginUser);
        } else {
            log.debug("无登录态访问 uri=" + request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(Constants.AUTH_HEADER);
        if (header != null && header.startsWith(Constants.TOKEN_PREFIX)) {
            return header.substring(Constants.TOKEN_PREFIX.length());
        }
        return header;
    }
}
