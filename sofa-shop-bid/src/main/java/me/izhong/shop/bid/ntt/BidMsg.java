package me.izhong.shop.bid.ntt;

import lombok.Getter;
import lombok.Setter;
import me.izhong.shop.bid.frame.BidContext;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
public class BidMsg {

	public BidMsg(BidContext context, String payload) {
		this.context = context;
		this.payload = payload;
	}

	public BidMsg(BidContext context, String payload,String uri, String auth, String remoteIp) {
		this.context = context;
		this.uri = uri;
		this.payload = payload;
		this.auth = auth;
		this.remoteIP = remoteIp;
	}

	private BidContext context;
	private String payload;
	private String uri;
	private String auth;
	private String remoteIP;

	public boolean isHeartBeat() {
		return StringUtils.isEmpty(payload);
	}
}
