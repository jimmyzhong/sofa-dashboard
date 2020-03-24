package me.izhong.shop.bid.service;

import lombok.Getter;
import lombok.Setter;
import me.izhong.shop.bid.rat.RateLimitClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    @Getter
    @Setter
    private volatile int bidLimit = 5;

    @Autowired
    private RateLimitClient rateLimitClient;

    public boolean acquireBid(String key){
        boolean isAll = rateLimitClient.isAllowed(key,bidLimit,bidLimit);
        return isAll;
    }

}
