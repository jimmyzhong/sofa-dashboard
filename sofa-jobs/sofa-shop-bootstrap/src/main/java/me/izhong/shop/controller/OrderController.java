package me.izhong.shop.controller;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Predicates;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dto.order.OrderFullDTO;
import me.izhong.shop.dto.order.SubmitOrderRequest;
import me.izhong.shop.dto.order.SubmitOrderResponse;
import me.izhong.shop.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.service.IOrderService;

import java.time.LocalDateTime;

import static me.izhong.shop.consts.OrderStateEnum.EXPIRED;
import static me.izhong.shop.consts.OrderStateEnum.getCommentByState;

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
    public SubmitOrderResponse submitOrder(@ApiParam(required = true, type = "object", value = "下单请求, like: \n{" +
			"  \"cartItems\": [00001,00002]," +
			"  \"addressId\": 0001" +
			"}") @RequestBody SubmitOrderRequest submitOrder, HttpServletRequest request) {
    	if (submitOrder.getCartItems() == null || submitOrder.getCartItems().size() == 0) {
    		throw BusinessException.build("订单内容为空");
		}
    	if (submitOrder.getAddressId() == null) {
    		throw BusinessException.build("请指定送货地址");
		}
    	Long userId = getCurrentUserId(request);
    	Order order = orderService.submit(userId, submitOrder.getAddressId(), submitOrder.getCartItems());
		if (LocalDateTime.now().isAfter(order.getCreateTime().plusMinutes(30))) {
			order.setStatus(EXPIRED.getState());
		}
		return SubmitOrderResponse.builder().orderNo(order.getOrderSn())
				.status(getCommentByState(order.getStatus())).build();
    }

    @PostMapping(value = "/confirmOrder")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="确认收货", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public void confirmOrder(@RequestBody SubmitOrderRequest orderRequest, HttpServletRequest request) {
		if(orderRequest.getOrderNo()==null) {
			throw BusinessException.build("订单号不能为空");
		}
		orderService.confirm(getCurrentUserId(request), orderRequest.getOrderNo());
	}

    @PostMapping(value = "/cancelOrder")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="用户取消订单", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
    public void cancelOrder(@RequestBody SubmitOrderRequest orderRequest, HttpServletRequest request) {
		if(orderRequest.getOrderNo()==null) {
			throw BusinessException.build("订单号不能为空");
		}
		orderService.cancel(getCurrentUserId(request), orderRequest.getOrderNo());
    }

	@GetMapping(value = "/detail/{orderNo}")
	@ResponseBody
	@RequireUserLogin
	@ApiOperation(value="订单详情", httpMethod = "GET")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public OrderFullDTO detail(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
    	return orderService.findFullOrderByOrderNo(orderNo);
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
}
