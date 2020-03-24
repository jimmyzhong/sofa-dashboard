package me.izhong.shop.bid.test.sub;

import redis.clients.jedis.JedisPubSub;

public class MsgListener extends JedisPubSub {

    public MsgListener() {
    }

    @Override
    public void onMessage(String channel, String message) {       //收到消息会调用
        System.out.println(String.format("收到消息成功！ channel： %s, message： %s", channel, message));
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