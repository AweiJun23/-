package com.trace.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trace.common.domain.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 登录账号 */
    private String username;

    /** BCrypt 加密后的密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色：FARMER / BUYER / ADMIN */
    private String role;

    /** 手机号（APP 端快捷登录用） */
    private String phone;

    /** 所属合作社 id */
    private Long coopId;

    /** 最近一次登录渠道：MINIAPP / APP / WEB */
    private String channel;

    /** 微信 openid（小程序登录） */
    private String openid;

    /** 状态：0-禁用 1-正常 */
    private Integer status;

    private LocalDateTime lastLoginTime;
}
