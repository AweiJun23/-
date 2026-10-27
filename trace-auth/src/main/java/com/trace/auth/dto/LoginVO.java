package com.trace.auth.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * 登录结果
 */
@Data
@Builder
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT 令牌 */
    private String token;
    /** 过期时间（毫秒） */
    private Long expireIn;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String channel;
    private Long coopId;
}
