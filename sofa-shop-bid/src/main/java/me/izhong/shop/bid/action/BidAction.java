package me.izhong.shop.bid.action;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.ann.ActionNode;
import me.izhong.shop.bid.frame.*;
import me.izhong.shop.bid.pojo.BidResponse;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActionNode(name = "测试服务",url = "/test")
public class BidAction implements IActionNode {

    @Override
    public void process(BidContext context, IFilterCallback callback) throws BusinessException {
        log.info("start service");
        BidResponse bidResponse = new BidResponse();
        bidResponse.setMessage("tttt");
        context.setResponse(bidResponse);
        callback.onPostProcess(context);
    }
}
