package me.izhong.shop.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.izhong.shop.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

public class CacheUtil {

    public static RedisTemplate getRedisTemplate(){
        return SpringUtil.getBean(StringRedisTemplate.class);
    }

    private static final String SESSION_PREFIX = "shop.user.{";
    private static final String SESSION_SUFFIX = "}.token";
    private static final int SESSION_TIMEOUT = 7200;

    public static void setSessionInfo(String key, SessionInfo sessionInfo) {
        ValueOperations ops = getRedisTemplate().opsForValue();
        String s = JSON.toJSONString(sessionInfo);
        ops.set(SESSION_PREFIX + key + SESSION_SUFFIX,s,SESSION_TIMEOUT, TimeUnit.SECONDS);
    }

    public static SessionInfo getSessionInfo(String key) {
        ValueOperations ops = getRedisTemplate().opsForValue();
        Object o = ops.get(SESSION_PREFIX + key + SESSION_SUFFIX);
        if(o == null)
            return null;
        getRedisTemplate().expire(SESSION_PREFIX + key + SESSION_SUFFIX,SESSION_TIMEOUT, TimeUnit.SECONDS);
        return JSONObject.parseObject(o.toString(),SessionInfo.class);
    }

    public static SessionInfo getSessionInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(StringUtils.isBlank(token)) {
            return null;
        }
        return getSessionInfo(token);
    }

    /**
     * 短信换成
     * @param key
     * @param value
     */
    public static void setSmsInfo(String key, String value) {
        ValueOperations ops = getRedisTemplate().opsForValue();
        ops.set("_sms_" + key,value,120, TimeUnit.SECONDS);
    }

    public static String getSmsInfo(String key) {
        ValueOperations ops = getRedisTemplate().opsForValue();
        return (String)ops.get("_sms_"  + key);
    }
}
