package me.izhong.shop.bid.rat;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidUploadInfo;
import me.izhong.jobs.model.bid.BidUserInfo;
import me.izhong.shop.bid.bean.RedisBidResponse;
import me.izhong.shop.bid.pojo.BidQueryItem;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
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

    private static final String sp1 = "##";
    private static final String sp2 = ",,";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    @Qualifier("lua_bid")
    RedisScript<List> bidScript;

    @Resource
    @Qualifier("lua_bid_query")
    RedisScript<List> bidQueryScript;

    @Resource
    @Qualifier("lua_bid_upload")
    RedisScript<List> bidUploadScript;

    @Resource
    @Qualifier("lua_bid_update")
    RedisScript<List> bidUpdateScript;

    public RedisBidResponse checkBidResult(String id, int tps, int cap) {
        // 每秒能够通过的请求数
        int replenishRate = tps;
        // 最大流量
        int burstCapacity =  cap;

        // 组装Lua脚本的KEY
        List<String> keys = getBidLimitKeys(id);
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

    public List<BidQueryItem> poolBidItems(String bidId, long start, long max) {
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

    public Boolean setBidInfo(BidUploadInfo bidUploadInfo) {
        String bidKey = bidUploadInfo.getBidId().toString();
        List<String> keys = getBidActionKeys(bidKey);

        Long bidId = bidUploadInfo.getBidId();
        Long startPrice = bidUploadInfo.getStartPrice();
        Long endPrice = bidUploadInfo.getEndPrice();
        Long stepPrice = bidUploadInfo.getStepPrice();
        String overPrice = (bidUploadInfo.getOverPrice() == null || bidUploadInfo.getOverPrice().booleanValue()) ? "true" : "false";
        Date startTime = bidUploadInfo.getStartTime();
        Date endTime = bidUploadInfo.getEndTime();

        String bidInfoString = new StringBuilder().append(bidId).append(sp1).append(startPrice).append(sp1)
                .append(endPrice).append(sp1 ).append(stepPrice).append(sp1).append(overPrice )
        .append(sp1 ).append( startTime.getTime()/1000).append(sp1 ).append(endTime.getTime()/1000).toString();

        StringBuilder userInfoBuilder = new StringBuilder();

        List<BidUserInfo> users = bidUploadInfo.getUsers();
        if( users!= null) {
            for(int i=0;i<users.size();i++) {
                BidUserInfo e = users.get(i);
                String avatar = e.getAvatar();
                String nickName = e.getNickName();
                RegExUtils.replaceAll(avatar,"##","");
                RegExUtils.replaceAll(avatar,",,","");
                RegExUtils.replaceAll(nickName,",,","");
                RegExUtils.replaceAll(nickName,",,","");
                userInfoBuilder.append(e.getUserId()).append(sp2).append(avatar).append(sp2).append(nickName);
                if(i < users.size() - 1)
                    userInfoBuilder.append(sp1);
            };
        }
        String userInfoString = userInfoBuilder.toString();

        String[] scriptArgs = new String[]{bidInfoString,userInfoString};

        List<String> rt = redisTemplate.execute(bidUploadScript, keys, scriptArgs);
        log.info("redis setBidInfo return {}", rt.toString());
        return rt != null && rt.size() > 0 && Long.valueOf(rt.get(0)).longValue() >=0 ;
    }

    public BidDownloadInfo getBidInfo(String bidKey) {
        List<String> keys = getBidActionKeys(bidKey);
        String[] scriptArgs = new String[]{"getInfo",""};
        List<String> rt = redisTemplate.execute(bidUpdateScript, keys, scriptArgs);
        log.info("redis getBidInfo return {}", rt.toString());
        long code = Long.valueOf(rt.get(0)).longValue();
        if(code < 0 && rt.size() <= 1)
            return null;
        //    resultValue = startPrice .. "##" .. endPrice.. "##" .. stepPrice.. "##" ..
        //    overPrice.. "##" .. startTime .. "##" .. endTime.. "##" .. createTime
        String data = rt.get(1);
        String[] bid = data.split("##",-1);
        BidDownloadInfo bidDownloadInfo = new BidDownloadInfo();
        bidDownloadInfo.setBidId(Long.valueOf(bid[0]));
        bidDownloadInfo.setStartPrice(Long.valueOf(bid[1]));
        bidDownloadInfo.setEndPrice(Long.valueOf(bid[2]));
        bidDownloadInfo.setStepPrice(Long.valueOf(bid[3]));
        bidDownloadInfo.setOverPrice(StringUtils.equals(bid[4],"Y"));
        bidDownloadInfo.setStartTime(new Date(Long.valueOf(bid[5])*1000));
        bidDownloadInfo.setEndTime(new Date(Long.valueOf(bid[6])*1000));
        bidDownloadInfo.setCreateTime(new Date(Long.valueOf(bid[7])*1000));
        bidDownloadInfo.setCanBid(StringUtils.equals(bid[8],"Y"));
        bidDownloadInfo.setIsOver(StringUtils.equals(bid[9],"Y"));
        return bidDownloadInfo;
    }

    public Boolean startBid(String bidKey) {
        List<String> keys = getBidActionKeys(bidKey);
        String[] scriptArgs = new String[]{"startBid",""};
        List<String> rt = redisTemplate.execute(bidUpdateScript, keys, scriptArgs);
        log.info("redis startBid return {}", rt.toString());
        long code = Long.valueOf(rt.get(0)).longValue();
        return code >=0 ;
    }

    public Boolean stopBid(String bidKey) {
        List<String> keys = getBidActionKeys(bidKey);
        String[] scriptArgs = new String[]{"stopBid",""};
        List<String> rt = redisTemplate.execute(bidUpdateScript, keys, scriptArgs);
        log.info("redis stopBid return {}", rt.toString());
        long code = Long.valueOf(rt.get(0)).longValue();
        return code >=0 ;
    }

    public Boolean addBidUsers(String bidKey , List<BidUserInfo> users) {
        List<String> keys = getBidActionKeys(bidKey);
        StringBuilder userInfoBuilder = new StringBuilder();
        if( users!= null) {
            for(int i=0;i<users.size();i++) {
                BidUserInfo e = users.get(i);
                String avatar = e.getAvatar();
                String nickName = e.getNickName();
                RegExUtils.replaceAll(avatar,"##","");
                RegExUtils.replaceAll(avatar,",,","");
                RegExUtils.replaceAll(nickName,",,","");
                RegExUtils.replaceAll(nickName,",,","");
                userInfoBuilder.append(e.getUserId()).append(sp2).append(avatar).append(sp2).append(nickName);
                if(i < users.size() - 1)
                    userInfoBuilder.append(sp1);
            };
        }
        String userInfoString = userInfoBuilder.toString();

        String[] scriptArgs = new String[]{"addUsers",userInfoString};

        List<String> rt = redisTemplate.execute(bidUpdateScript, keys, scriptArgs);

        log.info("redis setBidInfo return {}", rt.toString());
        long code = Long.valueOf(rt.get(0)).longValue();
        String result = rt != null && rt.size() > 1 ? rt.get(1).toString() : "";
        return code >=0 ;
    }

    List<String> getBidLimitKeys(String bidId) {
        String prefix = "bid.goods.{" + bidId;
        String tokenKey = prefix + "}.tokens"; // request_rate_limiter.{id}.tokens
        String timestampKey = prefix + "}.timestamp";// request_rate_limiter.{id}.timestamp
        String bidKey = prefix + "}.info";// request_rate_limiter.{id}.timestamp
        String bidList = prefix + "}.list";// request_rate_limiter.{id}.timestamp
        return Arrays.asList(tokenKey, timestampKey,bidKey,bidList);
    }

    List<String> getBidActionKeys(String bidId) {
        String prefix = "bid.goods.{" + bidId;
        String bidKey = prefix + "}.info";
        String bidList = prefix + "}.users";
        return Arrays.asList(bidKey,bidList);
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