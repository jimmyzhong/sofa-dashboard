package me.izhong.shop.controller;

import javax.servlet.http.HttpServletRequest;

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

@Slf4j
@Controller
@RequestMapping(value = "/api/order")
public class OrderController {
	
	@Autowired
	private IOrderService orderService;

    @PostMapping(value = "/submitOrder")
    @ResponseBody
    public void submitOrder(@RequestBody String body, HttpServletRequest request) {
    	Long userId = getCurrentUserId(request);
    	if (userId == null) {
    		throw BusinessException.build("用户未登录");
    	}
    	orderService.submit(userId, body);
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
}
