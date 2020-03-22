package me.izhong.shop.bid.ntt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.frame.ITask;
import me.izhong.shop.bid.util.TraceUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Slf4j
public class RequestHandler extends ChannelInboundHandlerAdapter {

	private NettyInvokeService nettyInvokeService;
	private NttTaskExecutor nttTaskExecutor;

	public RequestHandler(NettyInvokeService nettyInvokeService,
						  NttTaskExecutor nttTaskExecutor) {
		this.nettyInvokeService = nettyInvokeService;
		this.nttTaskExecutor = nttTaskExecutor;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		TraceUtil.initTrace();
		log.info("R请求：{}", ctx.channel().remoteAddress());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		TraceUtil.clearTrace();
		log.info("连接关闭：{}", ctx.channel().remoteAddress());
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object obj)
			throws Exception {
		if (obj instanceof BidMsg) {
			final BidMsg msg = (BidMsg) obj;
			if (StringUtils.isEmpty(msg.getPayload())) {
				ctx.channel().writeAndFlush(
						new BidMsg(msg.getContext(), ""));
			} else {
				nttTaskExecutor.executeImmediately(msg.getContext(), new ITask() {
					public void run() {
						nettyInvokeService.processInvoke(msg.getContext(),
								ctx.channel());
					}
				});
			}
		} else {
			throw new Exception("消息类型错误");
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		TraceUtil.clearTrace();
		if (cause instanceof IOException) {
			log.error("通讯异常：{}, {}", cause.getMessage(), ctx.channel()
					.remoteAddress());
		} else if (cause instanceof DecoderException) {
			Throwable c = cause.getCause();
			if (c != null)
				cause = c;
			log.error("解码异常：{}, {}", cause.getMessage(), ctx.channel()
					.remoteAddress());
		} else {
			log.error("垃圾异常：{}", ctx.channel().remoteAddress(), cause);
		}

		ctx.channel().close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
			TraceUtil.clearTrace();
			log.info("连接超时，强制关闭：{}", ctx.channel().remoteAddress());
			ctx.channel().close();
		}
	}
}
