package me.izhong.shop.bid.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.util.SpringUtil;
import redis.clients.jedis.JedisPubSub;

@Slf4j
public class MsgListener extends JedisPubSub {

    public MsgListener() {
    }

    @Override
    public void onMessage(String channel, String message) {       //收到消息会调用
        System.out.println(String.format("收到消息成功！ channel： %s, message： %s", channel, message));

        RateLimitService rateLimitService = SpringUtil.getBean(RateLimitService.class);
        JSONObject jsonObject = JSON.parseObject(message);
        int setBidLimit = jsonObject.getIntValue("setBidLimit");
        if(setBidLimit > 0 && setBidLimit <= 50) {
            log.info("设置报价tps{}",setBidLimit);
            rateLimitService.setBidLimit(setBidLimit);
        }
        //this.unsubscribe();
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {    //订阅频道会调用
        System.out.println(String.format("订阅频道成功！ channel： %s, subscribedChannels %d",
                channel, subscribedChannels));
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {   //取消订阅会调用
        System.out.println(String.format("取消订阅频道！ channel： %s, subscribedChannels： %d",
                channel, subscribedChannels));

    }
}