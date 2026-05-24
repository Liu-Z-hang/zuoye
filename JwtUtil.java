package com.example.studyroomserver.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    // 密钥（生产环境建议改成更复杂的字符串，不要硬编码）
    private static final String SECRET_KEY = "studyroom_jwt_secret_key_20260522_aes256";
    // Token过期时间：2小时（单位：毫秒）
    private static final long EXPIRATION_TIME = 2 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成Token
     * @param userId 用户ID
     * @return 生成的JWT令牌
     */
    public String generateToken(Long userId) {
        return Jwts.builder()
                // 设置Token主体为用户ID
                .setSubject(String.valueOf(userId))
                // 设置过期时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                // 使用HS256算法签名
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析Token，获取用户ID
     * @param token 前端传来的Token
     * @return 用户ID
     */
    public Long parseToken(String token) {
        // 去掉Token前面的"Bearer "前缀
        String realToken = token.replace("Bearer ", "");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(realToken)
                .getBody();

        return Long.valueOf(claims.getSubject());
    }

    /**
     * 验证Token是否有效
     * @param token 前端传来的Token
     * @return true有效，false无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            // Token过期、签名错误、格式错误都会抛出异常
            return false;
        }
    }
}