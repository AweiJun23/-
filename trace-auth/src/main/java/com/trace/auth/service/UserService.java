package com.trace.auth.service;

import com.trace.auth.dto.LoginVO;
import com.trace.auth.entity.SysUser;
import java.util.List;

/**
 * 用户权限服务领域接口
 */
public interface UserService {

    /** 账号注册 */
    Long register(com.trace.auth.dto.RegisterRequest request);

    /** 账号密码登录 */
    LoginVO login(com.trace.auth.dto.LoginRequest request);

    /** 微信小程序授权登录 */
    LoginVO wechatLogin(com.trace.auth.dto.WechatLoginRequest request);

    /** 按 id 查询用户 */
    SysUser getById(Long id);

    /** 查询某个收购商名下的农户 */
    List<SysUser> listFarmers(Long coopId);

    /** 查询全部农户 */
    List<SysUser> listAllFarmers();

    /** 删除农户（软删除，逻辑删除 deleted=1） */
    void deleteFarmer(Long id);
}
