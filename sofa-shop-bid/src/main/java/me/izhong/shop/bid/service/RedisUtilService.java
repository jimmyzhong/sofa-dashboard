package me.izhong.shop.bid.service;

import lombok.Getter;
import lombok.Setter;
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

    public List<BidQueryItem> getBidItems(String key, Long start) {
        return redisBidClient.getBidItems(key,start,maxPageSize);
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
