package com.trace.common.context;

import com.trace.common.domain.LoginUser;

/**
 * 当前登录用户上下文（ThreadLocal）
 * <p>
 * 由各微服务的拦截器 AuthInterceptor 写入，Controller / Service 直接读取。
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUserId();
    }

    public static String getRole() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getRole();
    }

    /**
     * 无登录态时（如 MQ 消费者内部调用）返回系统标识
     */
    public static Long getUserIdOrDefault(long defaultId) {
        Long id = getUserId();
        return id == null ? defaultId : id;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
