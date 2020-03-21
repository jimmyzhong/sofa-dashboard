package me.izhong.shop.bid.ntt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class ResponseLogger extends MessageToMessageEncoder<BidMsg> {

	@Override
	protected void encode(ChannelHandlerContext ctx, BidMsg in,
                          List<Object> out) throws Exception {
		String msg = in.getPayload();
		if (StringUtils.isEmpty(msg)) {
			log.debug("心跳应答: {}", ctx.channel().remoteAddress());
		} else {
			log.info("应答报文: {}\n{}", ctx.channel().remoteAddress(), msg);
		}
		out.add(in);
	}

}
