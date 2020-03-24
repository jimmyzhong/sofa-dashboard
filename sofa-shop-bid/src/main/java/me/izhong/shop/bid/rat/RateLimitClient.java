package me.izhong.shop.bid.rat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class RateLimitClient {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    StringRedisTemplate redisTemplate;

//    @Resource
//    @Qualifier("rateLimitLua")
    RedisScript<Long> rateLimitScript;

    public RateLimitClient(StringRedisTemplate redisTemplate, RedisScript<Long> rateLimitScript) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
    }

    public boolean isAllowed(String id, int tps, int cap) {
        //routeId是ip地址，id是使用KeyResolver获取的限流维度id，比如说基于uri,IP或者用户等等。
        // 每秒能够通过的请求数
        int replenishRate = tps;
        // 最大流量
        int burstCapacity =  cap;

        // 组装Lua脚本的KEY
        List<String> keys = getKeys(id);
        // 组装Lua脚本需要的参数，1是指一次获取一个令牌
        String[] scriptArgs = new String[]{replenishRate + "",
                burstCapacity + "", Instant.now().getEpochSecond() + "", "1"};
        // 调用Redis，tokens_left = redis.eval(SCRIPT, keys, args)
        long v  = redisTemplate.execute(rateLimitScript, keys, scriptArgs) ;
        return v > 0;

    }

    List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    public RateLimitResult init(String key, RateLimitVo rateLimitInfo){
        return exec(key, RateLimitMethod.init,
                rateLimitInfo.getInitialPermits(),
                rateLimitInfo.getMaxPermits(),
                rateLimitInfo.getInterval(),
                key);
    }

    public RateLimitResult modify(String key, RateLimitVo rateLimitInfo){
        return exec(key, RateLimitMethod.modify, key,
                rateLimitInfo.getMaxPermits(),
                rateLimitInfo.getInterval());
    }

    public RateLimitResult delete(String key){
        return exec(key, RateLimitMethod.delete);
    }

    public RateLimitResult acquire(String key){
        return acquire(key, 1);
    }

    public RateLimitResult acquire(String key, Integer permits){
        return exec(key, RateLimitMethod.acquire, permits);
    }

    /**
     * 执行redis的具体方法，限制method,保证没有其他的东西进来
     * @param key
     * @param method
     * @param params
     * @return
     */
    private RateLimitResult exec(String key, RateLimitMethod method, Object... params){
        try {
            Long timestamp = getRedisTimestamp();
            String[] allParams = new String[params.length + 2];
            allParams[0] = method.name();
            allParams[1] = timestamp.toString();
            for(int index = 0;index < params.length; index++){
                allParams[2 + index] = params[index].toString();
            }
            Long result = redisTemplate.execute(rateLimitScript,
                    Collections.singletonList(getKey(key)),
                    allParams);
            return RateLimitResult.getResult(result);
        } catch (Exception e){
            log.error("execute redis script fail, key:{}, method:{}",
                    key, method.name(), e);
            return RateLimitResult.ERROR;
        }
    }

    private Long getRedisTimestamp(){
        Long currMillSecond = redisTemplate.execute(
                (RedisCallback<Long>) redisConnection -> redisConnection.time()
        );
        return currMillSecond;
    }
    private String getKey(String key){
        return RATE_LIMIT_PREFIX + key;
    }
}