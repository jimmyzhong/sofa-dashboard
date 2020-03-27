package me.izhong.shop.bid.service;

import lombok.Getter;
import lombok.Setter;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidUploadInfo;
import me.izhong.shop.bid.bean.RedisBidResponse;
import me.izhong.shop.bid.pojo.BidQueryItem;
import me.izhong.shop.bid.rat.RedisBidClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Getter
@Setter
public class RedisUtilService {

    private volatile int bidLimit = 5;

    private volatile long maxPageSize = 15;

    @Autowired
    private RedisBidClient redisBidClient;

    @Autowired
    private JedisPool jedisPool;

    public RedisBidResponse acquireBid(String key) {
        return redisBidClient.checkBidResult(key,bidLimit,bidLimit);
    }

    /**
     * 客户端获取列表
     * @param key
     * @param start
     * @return
     */
    public List<BidQueryItem> poolBidItems(String key, Long start) {
        return redisBidClient.poolBidItems(key,start,maxPageSize);
    }

    public List<BidQueryItem> poolBidItems(String key, Long start, Long max) {
        return redisBidClient.poolBidItems(key,start,max);
    }

    public Boolean setBidInfo(BidUploadInfo bidUploadInfo) {
        return redisBidClient.setBidInfo(bidUploadInfo);
    }

    public BidDownloadInfo getBidInfo(String key) {
        return redisBidClient.getBidInfo(key);
    }

    public Boolean stopBid(String key) {
        return redisBidClient.stopBid(key);
    }

    public Boolean startBid(String key) {
        return redisBidClient.stopBid(key);
    }

    public List<BidQueryItem> getAllBidItems(String key) {
        List<String> keys = redisBidClient.getBidQueryKeys(key);
        Map<String, String> maps = getJedis().hgetAll(keys.get(0));
        List<BidQueryItem> items = new ArrayList<>();
        maps.forEach((k,v) -> {
            String[] values = v.split(",");
            items.add(new BidQueryItem(Long.valueOf(values[0]),Long.valueOf(values[1])));
        });
        return items;
    }

    private Jedis getJedis(){
        return jedisPool.getResource();
    }


}
