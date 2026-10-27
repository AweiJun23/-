package com.trace.common.constant;

/**
 * 系统全局常量
 */
public class Constants {

    /** JWT 请求头 */
    public static final String AUTH_HEADER = "Authorization";
    /** JWT 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    /** 网关透传的用户信息请求头 */
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    /** Token 有效期：24 小时 */
    public static final long TOKEN_TTL_MILLIS = 24 * 60 * 60 * 1000L;

    /** RBAC 角色 */
    public static final String ROLE_FARMER = "FARMER";
    public static final String ROLE_BUYER = "BUYER";
    public static final String ROLE_ADMIN = "ADMIN";

    /** 登录渠道：小程序 / APP / WEB */
    public static final String CHANNEL_MINIAPP = "MINIAPP";
    public static final String CHANNEL_APP = "APP";
    public static final String CHANNEL_WEB = "WEB";

    /** 批次状态 */
    public static final String BATCH_STATUS_GROWING = "GROWING";
    public static final String BATCH_STATUS_HARVESTED = "HARVESTED";
    public static final String BATCH_STATUS_STORED = "STORED";

    /** 存证业务类型 */
    public static final String CHAIN_BIZ_FARMING = "FARMING_RECORD";
    public static final String CHAIN_BIZ_CARBON = "CARBON_REPORT";
    public static final String CHAIN_BIZ_BATCH = "PRODUCT_BATCH";
}
