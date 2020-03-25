package me.izhong.shop.bid.rat;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.bean.RedisBidResponse;
import me.izhong.shop.bid.pojo.BidQueryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class RedisBidClient {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    @Qualifier("lua_bid")
    RedisScript<List> bidScript;

    @Resource
    @Qualifier("lua_bid_query")
    RedisScript<List> bidQueryScript;

    public RedisBidResponse checkBidResult(String id, int tps, int cap) {
        // 每秒能够通过的请求数
        int replenishRate = tps;
        // 最大流量
        int burstCapacity =  cap;

        // 组装Lua脚本的KEY
        List<String> keys = getBidKeys(id);
        String[] scriptArgs = new String[]{replenishRate + "",
                burstCapacity + "", Instant.now().getEpochSecond() + "", "1"};
        // 调用Redis，tokens_left = redis.eval(SCRIPT, keys, args)
        List<String> rt = redisTemplate.execute(bidScript, keys, scriptArgs);
        log.info("redis return {}", rt.toString());

        Map<String,String> map = new HashMap<>();
        for(int i = 0;i<rt.size();i=i+2) {
            map.put(rt.get(i),rt.get(i+1));
        }

        RedisBidResponse redisBidResponse = new RedisBidResponse();
        Long allow = Long.valueOf(map.get("allow"));
        redisBidResponse.setAllow(allow);
        if(allow.longValue() >= 0) {
            redisBidResponse.setPrice(Long.valueOf(map.get("price")));
            redisBidResponse.setSeqId(Long.valueOf(map.get("seqId")));
        }

        return redisBidResponse;
    }

    public List<BidQueryItem> getBidItems(String bidId, long start, long max) {
        List<String> keys = getBidQueryKeys(bidId);
        String[] scriptArgs = new String[]{start + "",max + ""};
        List<String> rt = redisTemplate.execute(bidQueryScript, keys, scriptArgs);
        log.info("redis query return {}", rt.toString());
        List<BidQueryItem> items = new ArrayList<>();
        if(rt != null ) {
            rt.forEach(e->{
                String[] data = e.split(",");
                items.add(new BidQueryItem(Long.valueOf(data[0]),Long.valueOf(data[1])));
            });
        }
        return items;
    }

    List<String> getBidKeys(String bidId) {
        String prefix = "bid.goods.{" + bidId;
        String tokenKey = prefix + "}.tokens"; // request_rate_limiter.{id}.tokens
        String timestampKey = prefix + "}.timestamp";// request_rate_limiter.{id}.timestamp
        String bidKey = prefix + "}.info";// request_rate_limiter.{id}.timestamp
        String bidList = prefix + "}.list";// request_rate_limiter.{id}.timestamp
        return Arrays.asList(tokenKey, timestampKey,bidKey,bidList);
    }

    public List<String> getBidQueryKeys(String bidId) {
        String prefix = "bid.goods.{" + bidId;
        String tokenKey = prefix + "}.list"; // request_rate_limiter.{id}.tokens
        return Arrays.asList(tokenKey);
    }

/*    List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens"; // request_rate_limiter.{id}.tokens
        String timestampKey = prefix + "}.timestamp";// request_rate_limiter.{id}.timestamp
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

    *//**
     * 执行redis的具体方法，限制method,保证没有其他的东西进来
     * @param key
     * @param method
     * @param params
     * @return
     *//*
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
    }*/
}