package me.izhong.shop.bid.action;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.ann.ActionNode;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.IFilterCallback;
import me.izhong.shop.bid.pojo.BidQueryItem;
import me.izhong.shop.bid.pojo.BidQueryResponse;
import me.izhong.shop.bid.pojo.BidRequest;
import me.izhong.shop.bid.pojo.BidResponse;
import me.izhong.shop.bid.service.RateLimitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActionNode(name = "报价查询", url = "/api/bid/query")
public class BidQueryAction implements IActionNode {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public void process(BidContext context, IFilterCallback callback) throws BusinessException {
        log.info("start bidquery service");
        JSONObject json = context.getJsonObjectRequest();
        boolean isQueryAll = StringUtils.equals(json.getString("queryAll"),"true");
        String bidId = json.getString("bidId");
        if(StringUtils.isBlank(bidId)) {
            throw BusinessException.build("bidId不能为空");
        }
        BidQueryResponse bidResponse = new BidQueryResponse();
        bidResponse.setCode(200);
        if(isQueryAll) {
            bidResponse.setMsg("查询全部报价");
            bidResponse.getBids().addAll(rateLimitService.getAllBidItems("bidId"));
        } else {
            bidResponse.setMsg("查询报价");
            Long start = json.getLong("start");
            if(start == null) {
                throw BusinessException.build("start不能为空");
            }
            if(start.longValue() < 1 || start.longValue() > 10000000) {
                throw BusinessException.build("start必须大于1小于10000000");
            }
            bidResponse.getBids().addAll(rateLimitService.getBidItems(bidId, start));
        }
        context.setResponse(bidResponse);
        callback.onPostProcess(context);
    }
}
