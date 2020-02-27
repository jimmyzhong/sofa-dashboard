package me.izhong.shop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.service.ICartItemService;
import me.izhong.shop.service.IUserService;

@Controller
@RequestMapping("/api/cart")
public class CartItemConroller {

	@Autowired
	private IUserService userService;
	@Autowired
	private ICartItemService cartItemService;

	@PostMapping(value = "/add")
    @ResponseBody
	public void add(@RequestBody CartItemParam cartItemParam) {
		cartItemService.add(cartItemParam);
	}

	@GetMapping(value = "/list")
    @ResponseBody
	public void list() {
		Long userId = null;
		cartItemService.list(userId);
	}

	@GetMapping(value = "/update/quantity")
    @ResponseBody
	public void updateQuantity(@RequestParam Long id, @RequestParam Integer quantity) {
		Long userId = null;
		cartItemService.updateQuantity(userId, id, quantity);
	}

	@PostMapping(value = "/delete")
    @ResponseBody
	public void delete(@RequestParam("ids") List<Long> ids) {
		Long userId = null;
		cartItemService.delete(userId, ids);
	}

	@PostMapping(value = "/clear")
    @ResponseBody
	public void clear() {
		Long userId = null;
		cartItemService.clear(userId);
	}
}
