package me.izhong.shop.bid.ntt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.ISecurityChecker;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class ContextBuilder extends MessageToMessageDecoder<BidMsg> {
	private ISecurityChecker checker;

	public ContextBuilder( ISecurityChecker checker) {
		this.checker = checker;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, BidMsg in,
                          List<Object> out) throws Exception {

		String msg = in.getPayload();
		String uri = in.getUri();
		JSONObject json;
		if (StringUtils.isBlank(msg)) {
			json = new JSONObject();
		} else {
			try {
				json = JSON.parseObject(msg);
			} catch (Exception e) {
				log.error("JSON解析出错: {}", ctx.channel().remoteAddress());
				json = new JSONObject();
			}
		}

		BidContext context = new BidContext();

		context.setUrl(uri);
		context.setRemoteIP(in.getRemoteIP());
		context.setServiceAcceptTime(System.currentTimeMillis());
		context.setExceptionHandler(new NettyExceptionHandler(ctx.channel()));
		context.setSecurityChecker(checker);
		context.setJsonObjectRequest(json);
		context.setJsonRequest(msg);

		in.setContext(context);

		out.add(in);
	}
}
