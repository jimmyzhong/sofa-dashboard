package me.izhong.shop.bid.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Slf4j
public class IPUtil {

	public static String getRemoteAddr(ChannelHandlerContext ctx,
                                       FullHttpRequest request) {
		try {
			String remoteAddr = request.headers().get("X-Forwarded-For");
			if (isEffective(remoteAddr) && (remoteAddr.indexOf(",") > -1)) {
				String[] array = remoteAddr.split(",");
				for (String element : array) {
					if (isEffective(element)) {
						remoteAddr = element;
						break;
					}
				}
			}
			if (!isEffective(remoteAddr)) {
				remoteAddr = request.headers().get("X-Real-IP");
			}
			if (!isEffective(remoteAddr)) {
				remoteAddr = getChannelRemoteIP(ctx);
			}

			if (isEffective(remoteAddr))
				return remoteAddr;
			else
				return "";
		} catch (Exception e) {
			log.error("get romote ip error,error message:" + e.getMessage());
			return "";
		}
	}

	public static String getChannelRemoteIP(ChannelHandlerContext ctx) {
		String remoteAddr = "";
		try {
			SocketAddress sockAddr = ctx.channel().remoteAddress();
			if (sockAddr instanceof InetSocketAddress) {
				InetAddress inetAddr = ((InetSocketAddress) sockAddr)
						.getAddress();
				remoteAddr = inetAddr.getHostAddress();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return remoteAddr;
	}

	/**
	 * 远程地址是否有效.
	 * 
	 * @param remoteAddr
	 *            远程地址
	 * @return true代表远程地址有效，false代表远程地址无效
	 */
	public static boolean isEffective(final String remoteAddr) {
		boolean isEffective = false;
		if ((null != remoteAddr) && (!"".equals(remoteAddr.trim()))
				&& (!"unknown".equalsIgnoreCase(remoteAddr.trim()))) {
			isEffective = true;
		}
		return isEffective;
	}
}
