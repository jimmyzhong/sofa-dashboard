package me.izhong.shop.bid.pojo;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import me.izhong.shop.bid.frame.BidContext;

@Setter
@Getter
public class ErrorResponse extends BaseResponse {
	public ErrorResponse(BidContext context, int code, String msg) {
		if (context != null) {
			JSONObject json = context.getJsonObjectRequest();
		}
		setCode(code);
		setMessage(msg);
	}
}
