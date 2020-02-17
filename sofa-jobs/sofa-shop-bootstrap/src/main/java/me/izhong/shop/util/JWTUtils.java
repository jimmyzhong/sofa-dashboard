package me.izhong.shop.util;

import com.alipay.common.tracer.core.utils.StringUtils;
import io.jsonwebtoken.*;
import me.izhong.shop.config.JWTProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static me.izhong.shop.response.ResponseCode.*;

public class JWTUtils {
    private static Logger log = LoggerFactory.getLogger(JWTUtils.class);
    public static final String AUTH_HEADER_KEY = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 解析jwt
     *
     * @param jsonWebToken
     * @param base64Security
     * @return
     */
    public static Claims parseJWT(String jsonWebToken, String base64Security) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
                    .parseClaimsJws(jsonWebToken).getBody();
            return claims;
        } catch (ExpiredJwtException eje) {
            log.error("===== Token过期 =====", eje);
            throw new RuntimeException(TOKEN_EXPIRED.getDescription());
        } catch (Exception e) {
            log.error("===== token解析异常 =====", e);
            throw new RuntimeException(TOKEN_INVALID.getDescription());
        }
    }

    /**
     * 构建jwt
     *
     * @param userId
     * @param username
     * @param role
     * @param jwtConfig
     * @return
     */
    public static String createJWT(String userId, String username, JWTProperties jwtConfig) {
        try {
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            //生成签名密钥
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtConfig.getSecret());
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
            String encryptId = Base64Utils.encodeToString(userId.getBytes());
            //添加构成JWT的参数
            JwtBuilder builder = Jwts.builder().setHeaderParam("type", "JWT")
                    .claim("userId", userId)
                    .setSubject(username)
                    .setIssuer(jwtConfig.getApplicationID())
                    .setIssuedAt(new Date())
                    .setAudience(jwtConfig.getName())
                    .signWith(signatureAlgorithm, signingKey);
            int TTLMillis = jwtConfig.getExpiresSecond();
            if (TTLMillis >= 0) {
                long expMillis = nowMillis + TTLMillis;
                Date exp = new Date(expMillis);
                builder.setExpiration(exp)
                        .setNotBefore(now);
            }
            return builder.compact();
        } catch (Exception e) {
            log.error("签名失败", e);
            throw new RuntimeException(SIGN_INVALID.getDescription());
        }
    }

    /**
     * 从token中获取用户名
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUsername(String token, String base64Security) {
        return parseJWT(token, base64Security).getSubject();
    }

    /**
     * 从token中获取用户ID
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUserId(String token, String base64Security) {
        String userId = parseJWT(token, base64Security).get("userId", String.class);
        return new String(Base64Utils.decodeFromString(userId), StandardCharsets.UTF_8);
    }

    /**
     * 是否已过期
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static boolean isExpiration(String token, String base64Security) {
        return parseJWT(token, base64Security).getExpiration().before(new Date());
    }

    public static void validateToken(String token, HttpServletRequest request, JWTProperties jwtConfig) {
        if(jwtConfig == null){
            BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            jwtConfig = (JWTProperties) factory.getBean("jwtConfig");
        }
        JWTUtils.parseJWT(token, jwtConfig.getSecret());
    }

    public static String getToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(JWTUtils.AUTH_HEADER_KEY);
        log.info("get authHeader= ", authHeader);
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JWTUtils.TOKEN_PREFIX)) {
            log.warn("user not logged in.");
            throw new RuntimeException(USER_NOT_LOGIN.getDescription());
        }
        return authHeader.substring(7);
    }
}
