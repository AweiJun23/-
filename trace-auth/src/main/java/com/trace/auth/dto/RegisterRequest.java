package com.trace.auth.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;

    private String realName;
    private String phone;
    private Long coopId;

    /** 注册渠道：MINIAPP / APP / WEB */
    private String channel;
}
