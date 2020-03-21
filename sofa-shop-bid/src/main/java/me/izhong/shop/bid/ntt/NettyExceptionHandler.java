package me.izhong.shop.bid.ntt;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.frame.AbstractExceptionHandler;
import me.izhong.shop.bid.frame.BidContext;

@Slf4j
public class NettyExceptionHandler extends AbstractExceptionHandler {
    private Channel channel;

    public NettyExceptionHandler(Channel channel) {
        this.channel = channel;
    }

    protected void response(BidContext context) {
        if (channel != null) {

            String msg = context.getJsonResponse();

            if (channel.isActive()) {
                channel.writeAndFlush(new BidMsg(context, msg));
            }
//            log.info("完成异步业务应答: {}, 耗时: {}",
//                    context.getBusinessNode() != null ? context.getBusinessNode().getName() : "",
//                    System.currentTimeMillis() - context.getServiceAcceptTime());
        }
    }
}
