package me.izhong.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dto.OrderParam;
import me.izhong.shop.service.IOrderService;

@Slf4j
@Controller
@RequestMapping(value = "/api/order")
public class OrderController {
	
	@Autowired
	private IOrderService orderService;

    @PostMapping(value = "/confirmOrder")
    @ResponseBody
	public void confirmOrder() {
		
	}

    @PostMapping(value = "/submitOrder")
    @ResponseBody
    public void submitOrder(@RequestBody OrderParam orderParam) {
    	
    }

    @PostMapping(value = "/cancelOrder")
    @ResponseBody
    public void cancelOrder(Long orderId) {
    	
    }
}
