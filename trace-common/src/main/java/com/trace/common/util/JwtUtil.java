package com.trace.common.util;

import com.trace.common.domain.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（HS256）
 * <p>
 * 说明：密钥通过 -Dtrace.jwt.secret 或配置注入，生产环境务必替换。
 */
public class JwtUtil {

    /** 默认密钥，生产环境需通过配置覆盖 */
    public static String SECRET = "agriTraceCarbonFootprintNotaryDefaultSecretKey2026";

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_CHANNEL = "channel";
    private static final String CLAIM_COOP_ID = "coopId";

    /**
     * 生成 Token
     */
    public static String generateToken(LoginUser user, long ttlMillis) {
        Map<String, Object> claims = new HashMap<>(8);
        claims.put(CLAIM_USER_ID, user.getUserId());
        claims.put(CLAIM_USERNAME, user.getUsername());
        claims.put(CLAIM_ROLE, user.getRole());
        claims.put(CLAIM_CHANNEL, user.getChannel());
        if (user.getCoopId() != null) {
            claims.put(CLAIM_COOP_ID, user.getCoopId());
        }
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static String generateToken(LoginUser user) {
        return generateToken(user, com.trace.common.constant.Constants.TOKEN_TTL_MILLIS);
    }

    /**
     * 解析 Token
     */
    public static Claims parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Token -> LoginUser
     */
    public static LoginUser toLoginUser(Claims claims) {
        if (claims == null) {
            return null;
        }
        LoginUser user = new LoginUser();
        Object userId = claims.get(CLAIM_USER_ID);
        if (userId instanceof Integer) {
            user.setUserId(((Integer) userId).longValue());
        } else if (userId instanceof Long) {
            user.setUserId((Long) userId);
        }
        user.setUsername(claims.get(CLAIM_USERNAME, String.class));
        user.setRole(claims.get(CLAIM_ROLE, String.class));
        user.setChannel(claims.get(CLAIM_CHANNEL, String.class));
        Object coopId = claims.get(CLAIM_COOP_ID);
        if (coopId instanceof Integer) {
            user.setCoopId(((Integer) coopId).longValue());
        } else if (coopId instanceof Long) {
            user.setCoopId((Long) coopId);
        }
        return user;
    }

    /**
     * 校验 Token 是否过期
     */
    public static boolean isExpired(Claims claims) {
        if (claims == null) {
            return true;
        }
        Date exp = claims.getExpiration();
        return exp == null || exp.before(new Date());
    }
}
