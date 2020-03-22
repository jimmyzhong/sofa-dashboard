package me.izhong.shop.bid.frame;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import me.izhong.shop.bid.action.IActionNode;
import me.izhong.shop.bid.pojo.BaseRequest;
import me.izhong.shop.bid.pojo.BaseResponse;

@Setter
@Getter
public class BidContext {

	private IActionNode ActionNode;
	private ISecurityChecker securityChecker;
	private String url;
	private Class reqClass;

	@Setter
	@Getter
	private long serviceAcceptTime = System.currentTimeMillis();

	@Setter
	@Getter
	private long serviceAcceptNanoTime = System.nanoTime();

	private BaseRequest request;

	private BaseResponse response;

	private String jsonRequest;

	private String requestSign;

	private JSONObject jsonObjectRequest;

	private IExceptionHandler exceptionHandler;

	private ContextMap contextMap = new ContextMap();

	private String traceId;

	private String remoteIP;

	private Throwable processException;


}
