package me.izhong.shop.bid;

import me.izhong.shop.bid.ntt.NttTaskExecutor;
import me.izhong.shop.bid.rat.RateLimitClient;
import me.izhong.shop.bid.service.RedisSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;

@SpringBootApplication(exclude = {JacksonAutoConfiguration.class},
        scanBasePackages = {"me.izhong"})
public class ShopBidApplication {

    @Autowired
    private NttTaskExecutor nttTaskExecutor;

    @Autowired
    private RateLimitClient rateLimitClient;

    @Autowired
    private JedisPool jedisPool;

    public static void main(String[] args) {
        SpringApplication.run(ShopBidApplication.class, args);

    }

    @PostConstruct
    void init(){
        boolean isAll = rateLimitClient.isAllowed("test2",3,3);
        System.out.println("xxxx" + isAll);


        RedisSubscriber subscriber = new RedisSubscriber(jedisPool);    //订阅者
        subscriber.start();


    }

}
