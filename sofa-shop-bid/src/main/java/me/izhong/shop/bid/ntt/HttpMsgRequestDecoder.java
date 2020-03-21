package me.izhong.shop.bid.ntt;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.util.IPUtil;

import java.nio.charset.Charset;
import java.util.List;

@Slf4j
public class HttpMsgRequestDecoder extends MessageToMessageDecoder<HttpObject> {
	private String charset;

	public HttpMsgRequestDecoder(String charset) {
		super();
		this.charset = charset;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, HttpObject in,
                          List<Object> out) throws Exception {
		FullHttpRequest request = (FullHttpRequest) in;

		String uri = request.uri();
		ByteBuf buf = request.content();
		String jsonStr = buf.toString(Charset.forName(charset));

		String auth = request.headers().get("Authorization");

		String remoteIp = IPUtil.getRemoteAddr(ctx, request);
		
		out.add(new BidMsg(null, jsonStr, uri, auth, remoteIp));
	}

}
