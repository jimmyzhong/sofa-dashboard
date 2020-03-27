package me.izhong.shop.bid.action;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.ann.ActionNode;
import me.izhong.shop.bid.bean.RedisBidResponse;
import me.izhong.shop.bid.frame.*;
import me.izhong.shop.bid.pojo.BidResponse;
import me.izhong.shop.bid.service.RedisUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActionNode(name = "测试服务", url = "/api/bid/acquire")
public class BidAction implements IActionNode {

    @Autowired
    private RedisUtilService redisUtilService;

    @Override
    public void process(BidContext context, IFilterCallback callback) throws BusinessException {
        log.info("start bid acquire service");
        JSONObject json = context.getJsonObjectRequest();

        RedisBidResponse rt = redisUtilService.acquireBid("test2");

        BidResponse resp = new BidResponse();
        if(rt.isSuccess()) {
            resp.setPrice(rt.getPrice());
            resp.setSeqId(rt.getSeqId());
            resp.setCode(200);
        } else {
            resp.setCode(rt.getAllow().intValue());
        }
        context.setResponse(resp);

        callback.onPostProcess(context);
    }
}
