package com.trace.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.auth.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 按 openid 查询（微信小程序登录）
     */
    @Select("SELECT * FROM sys_user WHERE openid = #{openid} AND deleted = 0 LIMIT 1")
    SysUser selectByOpenid(@Param("openid") String openid);

    /**
     * 按手机号查询（APP 端手机号验证码登录）
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0 LIMIT 1")
    SysUser selectByPhone(@Param("phone") String phone);
}
