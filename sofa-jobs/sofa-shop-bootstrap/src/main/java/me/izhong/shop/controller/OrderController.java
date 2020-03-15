package me.izhong.shop.controller;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Predicates;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.dto.order.OrderDTO;
import me.izhong.shop.dto.order.OrderFullDTO;
import me.izhong.shop.dto.order.SubmitOrderRequest;
import me.izhong.shop.dto.order.SubmitOrderResponse;
import me.izhong.shop.entity.Order;
import me.izhong.shop.service.impl.ResaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.service.IOrderService;

import java.time.LocalDateTime;

import static me.izhong.shop.consts.OrderStateEnum.*;

@Slf4j
@Controller
@AjaxWrapper
@RequestMapping(value = "/api/order")
@Api(value = "订单接口",description = "订单相关接口描述")
public class OrderController {
	
	@Autowired
	private IOrderService orderService;
	@Autowired
	private ResaleService resaleService;

	@Value("${order.expire.time}")
	private Long orderExpireMinutes;

    @PostMapping(value = "/submitOrder")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="购物车下单", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
    public SubmitOrderResponse submitOrder(@ApiParam(required = true, type = "object", value = "下单请求, like: \n{" +
			"  \"cartItems\": [00001,00002]," +
			"  \"addressId\": 0001" +
			"}") @RequestBody SubmitOrderRequest submitOrder, HttpServletRequest request) {
    	if (submitOrder.getCartItems() == null || submitOrder.getCartItems().size() == 0) {
    		throw BusinessException.build("订单内容为空");
		}

    	Long userId = getCurrentUserId(request);
    	Order order = orderService.submit(userId, submitOrder.getAddressId(), submitOrder.getCartItems());
		return SubmitOrderResponse.builder().orderNo(order.getOrderSn())
				.status(getCommentByState(order.getStatus()))
				.timeToPay(String.valueOf(orderExpireMinutes)).build();
    }

	@PostMapping(value = "/submitOrder/goods")
	@ResponseBody
	@RequireUserLogin
	@ApiOperation(value="直接购买", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public SubmitOrderResponse buy(@ApiParam(required = true, type = "object", value = "下单请求, like: \n{" +
			"  \"productId\": 0001," +
			"  \"productAttrId\": 0001," +
			"  \"quantity\": 2," +
			"  \"addressId\": 0001" +
			"}") @RequestBody SubmitOrderRequest submitOrder, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		Order order = orderService.submit(userId, submitOrder.getAddressId(), submitOrder.getProductId(),
						submitOrder.getProductAttrId(), submitOrder.getQuantity());
		return SubmitOrderResponse.builder().orderNo(order.getOrderSn())
				.status(getCommentByState(order.getStatus()))
				.timeToPay(String.valueOf(orderExpireMinutes)).build();
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

	@PostMapping(value = "/resaleOrder")
	@ResponseBody
	@RequireUserLogin
	@ApiOperation(value="申请寄售", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public void resaleOrder(@RequestBody SubmitOrderRequest orderRequest, HttpServletRequest request) {
		if(orderRequest.getOrderNo()==null) {
			throw BusinessException.build("订单号不能为空");
		}
		resaleService.resaleOrder(getCurrentUserId(request), orderRequest.getOrderNo());
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

	@PostMapping(value = "/list")
	@ResponseBody
	@RequireUserLogin
	@ApiOperation(value="用户订单列表", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public PageModel<OrderDTO> list(@RequestBody PageQueryParamDTO query, HttpServletRequest request) {
		return orderService.list(getCurrentUserId(request), query);
	}

	@PostMapping(value = "/updateExpires")
	public void update() {
    	orderService.updateExpiredOrders();
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
