package me.izhong.shop.bid.test.sub;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class TestPubSub {
    public static void main(String[] args) {
        // 连接redis服务端
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(),
                "r-bp1o1p8t1kg5squfftpd.redis.rds.aliyuncs.com", 6379,1000,"redis@test123");

        Publisher publisher = new Publisher(jedisPool);    //发布者
        publisher.start();

        Subscriber subscriber = new Subscriber(jedisPool);    //订阅者
        subscriber.start();


    }
}