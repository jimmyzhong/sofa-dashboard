package me.izhong.shop.bid.ntt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.frame.AbstractExceptionHandler;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.pojo.BaseResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NttExceptionHandler extends AbstractExceptionHandler {
    private Channel channel;

    public NttExceptionHandler(Channel channel) {
        this.channel = channel;
    }

    protected void response(BidContext context) {
        if (channel != null) {

            BaseResponse response = context.getResponse();

            Map map = new HashMap();
            map.put("data",response);
            map.put("code",response.getCode());
            map.put("msg",response.getMsg());

            if (channel.isActive()) {
                channel.writeAndFlush(new BidMsg(context, JSON.toJSONString(map)));
            }
//            log.info("完成异步业务应答: {}, 耗时: {}",
//                    context.getBusinessNode() != null ? context.getBusinessNode().getName() : "",
//                    System.currentTimeMillis() - context.getServiceAcceptTime());
        }
    }
}
