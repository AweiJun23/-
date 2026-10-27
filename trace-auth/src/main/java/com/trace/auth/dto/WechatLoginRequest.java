package com.trace.auth.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序授权登录请求
 * <p>
 * 真实环境：前端 wx.login 拿 code -> 后端调用 jscode2session 换 openid。
 * 本实现留出 code2session 的调用点，未配置 appid/secret 时使用 mock openid，便于本地联调。
 */
@Data
public class WechatLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "code 不能为空")
    private String code;

    /** 微信返回的加密用户信息（真实环境需解密 unionId） */
    private String encryptedData;
    private String iv;
}
