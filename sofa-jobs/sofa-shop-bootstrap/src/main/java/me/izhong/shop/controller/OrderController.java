package me.izhong.shop.controller;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.consts.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.dto.OrderParam;
import me.izhong.shop.service.IOrderService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "/api/order")
@Api(value = "订单接口",description = "订单相关接口描述")
public class OrderController {
	
	@Autowired
	private IOrderService orderService;

    @PostMapping(value = "/submitOrder")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="下单", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
    public void submitOrder(@ApiParam(required = true, type = "object", value = "下单请求, like: \n{" +
			"  \"cartItems\": [00001,00002]," +
			"  \"addressId\": 0001" +
			"}") @RequestBody SubmitOrderRequest submitOrder, HttpServletRequest request) {
    	if (submitOrder.cartItems == null || submitOrder.cartItems.size() == 0) {
    		throw BusinessException.build("订单内容为空");
		}
    	if (submitOrder.addressId == null) {
    		throw BusinessException.build("请指定送货地址");
		}
    	Long userId = getCurrentUserId(request);
    	orderService.submit(userId, submitOrder.addressId, submitOrder.cartItems);
    }

    @PostMapping(value = "/confirmOrder")
    @ResponseBody
	public void confirmOrder() {
		
	}

    @PostMapping(value = "/cancelOrder")
    @ResponseBody
    public void cancelOrder(Long orderId) {
    	
    }

    /**
     * 
     * @param request
     * @return
     */
	private Long getCurrentUserId(HttpServletRequest request) {
		SessionInfo session = CacheUtil.getSessionInfo(request);
		return session.getId();
	}


	public static class SubmitOrderRequest{
		List<Long> cartItems;
		Long addressId;
	}
}
