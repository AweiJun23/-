package com.trace.common.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户载体（JWT payload + 网关透传）
 */
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String role;
    /** 登录渠道：MINIAPP / APP / WEB */
    private String channel;
    /** 所属合作社 id */
    private Long coopId;

    public LoginUser() {
    }

    public LoginUser(Long userId, String username, String role, String channel, Long coopId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.channel = channel;
        this.coopId = coopId;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isBuyer() {
        return "BUYER".equals(role);
    }

    public boolean isFarmer() {
        return "FARMER".equals(role);
    }

    /** 是否拥有任一角色 */
    public boolean hasAnyRole(List<String> roles) {
        return role != null && roles != null && roles.contains(role);
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Long getCoopId() { return coopId; }
    public void setCoopId(Long coopId) { this.coopId = coopId; }
}
