package me.izhong.shop.bid.action;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.ann.ActionNode;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.IFilterCallback;
import me.izhong.shop.bid.ntt.NttTaskExecutor;
import me.izhong.shop.bid.rat.RedisBidClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Slf4j
@ActionNode(name = "发布配置", url = "/test/publish")
public class PublishAction implements IActionNode {

    @Autowired
    private NttTaskExecutor nttTaskExecutor;

    @Autowired
    private RedisBidClient redisBidClient;

    private volatile long s = 0;

    private volatile boolean start = true;

    @Autowired
    private JedisPool jedisPool;

    @Override
    public void process(BidContext context, IFilterCallback callback) throws BusinessException {
        log.info("start service");
        JSONObject json = context.getJsonObjectRequest();
        String data = json.getString("data");
        Jedis jedis = jedisPool.getResource();   //连接池中取出一个连接

        jedis.publish("datachannel", json.toJSONString());
        System.out.println(String.format("发布消息成功！ %s", json.toJSONString()));
        callback.onPostProcess(context);
    }

}
