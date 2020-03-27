package me.izhong.shop.bid.ntt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class RequestLogger extends MessageToMessageDecoder<BidMsg> {
	@Override
	protected void decode(ChannelHandlerContext ctx, BidMsg in,
                          List<Object> out) throws Exception {
		String msg = in.getPayload();

		if (StringUtils.isEmpty(msg)) {
			log.debug("收跳: {}", ctx.channel().remoteAddress());
		} else {
			log.info("收到：LIP={}, RIP={}\n{}", ctx.channel()
					.remoteAddress(), in.getRemoteIP(), msg);
		}
		out.add(in);
	}
}
