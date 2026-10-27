package com.trace.auth.controller;

import com.trace.auth.dto.LoginRequest;
import com.trace.auth.dto.LoginVO;
import com.trace.auth.dto.RegisterRequest;
import com.trace.auth.dto.WechatLoginRequest;
import com.trace.auth.entity.SysUser;
import com.trace.auth.service.UserService;
import com.trace.common.context.UserContext;
import com.trace.common.enums.ResultCode;
import com.trace.common.exception.BizException;
import com.trace.common.result.R;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户权限接口
 * <p>
 * 兼容三种登录方式：Web 后台账号密码、APP 手机号/账号密码、微信小程序授权。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** 注册 */
    @PostMapping("/register")
    public R<Long> register(@Valid @RequestBody RegisterRequest request) {
        return R.ok(userService.register(request));
    }

    /** 账号密码登录（APP / Web 后台） */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(userService.login(request));
    }

    /** 微信小程序授权登录 */
    @PostMapping("/wechat/login")
    public R<LoginVO> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return R.ok(userService.wechatLogin(request));
    }

    /** 当前登录用户信息 */
    @GetMapping("/info")
    public R<SysUser> info() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return R.ok(userService.getById(userId));
    }

    /** 收购商：查询名下农户 */
    @GetMapping("/farmers")
    public R<List<SysUser>> farmers() {
        com.trace.common.domain.LoginUser loginUser = UserContext.get();
        Long coopId = loginUser == null ? null : loginUser.getCoopId();
        return R.ok(userService.listFarmers(coopId));
    }

    /** 管理员：查询全部农户 */
    @GetMapping("/farmers/all")
    public R<List<SysUser>> allFarmers() {
        return R.ok(userService.listAllFarmers());
    }

    /** 删除农户（软删除） */
    @DeleteMapping("/farmers/{id}")
    public R<Boolean> deleteFarmer(@PathVariable("id") Long id) {
        userService.deleteFarmer(id);
        return R.ok(true);
    }

    /** 内部接口：供其它微服务 Feign 调用 */
    @GetMapping("/internal/{id}")
    public R<SysUser> internalGet(@PathVariable("id") Long id) {
        return R.ok(userService.getById(id));
    }
}
