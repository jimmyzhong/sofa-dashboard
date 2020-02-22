package me.izhong.shop.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.izhong.shop.util.SpringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

public class CacheUtil {

    public static RedisTemplate getRedisTemplate(){
        return SpringUtil.getBean(StringRedisTemplate.class);
    }

    private static final String SESSION_PREFIX = "_se_";
    private static final int SESSION_TIMEOUT = 3600;

    public static void setSessionInfo(String key, SessionInfo sessionInfo) {
        ValueOperations ops = getRedisTemplate().opsForValue();
        String s = JSON.toJSONString(sessionInfo);
        ops.set(SESSION_PREFIX + key,s,SESSION_TIMEOUT, TimeUnit.SECONDS);
    }

    public static SessionInfo getSessionInfo(String key) {
        ValueOperations ops = getRedisTemplate().opsForValue();
        Object o = ops.get(SESSION_PREFIX + key);
        if(o == null)
            return null;
        return JSONObject.parseObject(o.toString(),SessionInfo.class);
    }
}
