package com.trace.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trace.auth.dto.LoginRequest;
import com.trace.auth.dto.LoginVO;
import com.trace.auth.dto.RegisterRequest;
import com.trace.auth.dto.WechatLoginRequest;
import com.trace.auth.entity.SysUser;
import com.trace.auth.mapper.SysUserMapper;
import com.trace.auth.service.UserService;
import com.trace.common.constant.Constants;
import com.trace.common.domain.LoginUser;
import com.trace.common.enums.ResultCode;
import com.trace.common.exception.BizException;
import com.trace.common.util.HashUtil;
import com.trace.common.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户权限服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 微信小程序配置：留空则使用 mock openid，便于无 AppID 环境下联调 */
    @Value("${wechat.appid:}")
    private String wechatAppid;

    @Value("${wechat.secret:}")
    private String wechatSecret;

    public UserServiceImpl(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        validateRole(request.getRole());

        Long exist = countByUsername(request.getUsername());
        if (exist != null && exist > 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "账号已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole().toUpperCase());
        user.setPhone(request.getPhone());
        user.setCoopId(request.getCoopId());
        user.setChannel(defaultChannel(request.getChannel()));
        user.setStatus(1);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));

        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        if (!StringUtils.hasText(user.getPassword()) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }

        String channel = defaultChannel(request.getChannel());
        touchLogin(user, channel);
        return buildVO(user, channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wechatLogin(WechatLoginRequest request) {
        // code -> openid（真实环境调用微信 jscode2session）
        String openid = code2Openid(request.getCode());

        SysUser user = userMapper.selectByOpenid(openid);
        if (user == null) {
            // 首次微信授权：自动注册为农户
            user = new SysUser();
            user.setUsername("wx_" + openid.substring(0, Math.min(12, openid.length())));
            user.setRole(Constants.ROLE_FARMER);
            user.setRealName("微信农户");
            user.setOpenid(openid);
            user.setChannel(Constants.CHANNEL_MINIAPP);
            user.setStatus(1);
            user.setPassword(passwordEncoder.encode(HashUtil.sha256(openid).substring(0, 16)));
            userMapper.insert(user);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        touchLogin(user, Constants.CHANNEL_MINIAPP);
        return buildVO(user, Constants.CHANNEL_MINIAPP);
    }

    @Override
    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public List<SysUser> listFarmers(Long coopId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, Constants.ROLE_FARMER);
        if (coopId != null) {
            wrapper.eq(SysUser::getCoopId, coopId);
        }
        List<SysUser> list = userMapper.selectList(wrapper);
        list.forEach(u -> u.setPassword(null));
        return list;
    }

    @Override
    public List<SysUser> listAllFarmers() {
        return listFarmers(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFarmer(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农户不存在");
        }
        if (!Constants.ROLE_FARMER.equals(user.getRole())) {
            throw new BizException(ResultCode.FORBIDDEN, "仅允许删除农户账号");
        }
        // MyBatis-Plus 逻辑删除：自动更新 deleted=1
        userMapper.deleteById(id);
    }

    // ---------------- private ----------------

    /**
     * 微信 code 换取 openid
     * <p>
     * 生产环境需调用 https://api.weixin.qq.com/sns/jscode2session，
     * 这里保留调用点，未配置 appid/secret 时降级为 mock，保证系统可独立运行。
     */
    private String code2Openid(String code) {
        if (StringUtils.hasText(wechatAppid) && StringUtils.hasText(wechatSecret)) {
            // TODO 接入真实 WeChat jscode2session：appid + secret + code + grant_type=authorization_code
            // RestTemplate restTemplate = new RestTemplate();
            // String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wechatAppid
            //         + "&secret=" + wechatSecret + "&js_code=" + code + "&grant_type=authorization_code";
            // JsonNode node = restTemplate.getForObject(url, JsonNode.class);
            // return node.get("openid").asText();
            return HashUtil.sha256(wechatAppid + code).substring(0, 24);
        }
        return "mock_openid_" + HashUtil.sha256(code).substring(0, 16);
    }

    private Long countByUsername(String username) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    private void touchLogin(SysUser user, String channel) {
        SysUser updater = new SysUser();
        updater.setId(user.getId());
        updater.setLastLoginTime(LocalDateTime.now());
        updater.setChannel(channel);
        userMapper.updateById(updater);
    }

    private LoginVO buildVO(SysUser user, String channel) {
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole(), channel, user.getCoopId());
        String token = JwtUtil.generateToken(loginUser);
        return LoginVO.builder()
                .token(token)
                .expireIn(Constants.TOKEN_TTL_MILLIS)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .channel(channel)
                .coopId(user.getCoopId())
                .build();
    }

    private String defaultChannel(String channel) {
        if (!StringUtils.hasText(channel)) {
            return Constants.CHANNEL_APP;
        }
        return channel.toUpperCase();
    }

    private void validateRole(String role) {
        if (!StringUtils.hasText(role)) {
            throw new BizException(ResultCode.PARAM_ERROR, "角色不能为空");
        }
        String upper = role.toUpperCase();
        if (!Constants.ROLE_FARMER.equals(upper)
                && !Constants.ROLE_BUYER.equals(upper)
                && !Constants.ROLE_ADMIN.equals(upper)) {
            throw new BizException(ResultCode.PARAM_ERROR, "角色取值只能为 FARMER / BUYER / ADMIN");
        }
    }
}
