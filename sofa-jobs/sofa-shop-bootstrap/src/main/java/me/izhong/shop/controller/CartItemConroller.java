package me.izhong.shop.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.service.ICartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@AjaxWrapper
@RequestMapping("/api/cart")
@Api(value = "购物车相关接口",description = "购物车相关接口描述")
public class CartItemConroller {

	@Autowired
	private ICartItemService cartItemService;

	@PostMapping(value = "/add")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="添加到购物车",httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
	public void add(@RequestBody CartItemParam cartItemParam, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		cartItemParam.setUserId(userId);
		cartItemService.add(cartItemParam);
	}

	@GetMapping(value = "/list")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="购物车内容",httpMethod = "GET")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
	public List<CartItemParam> list(HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		//userService.findById(userId);

		return cartItemService.list(userId);
	}

	@GetMapping(value = "/update/quantity")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="修改购物车商品数量",httpMethod = "GET")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
	public void updateQuantity(@RequestParam Long id, @RequestParam Integer quantity, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		cartItemService.updateQuantity(userId, id, quantity);
	}

	@PostMapping(value = "/delete")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="删除购物车商品",httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
	public void delete(@RequestParam("ids") List<Long> ids, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		cartItemService.delete(userId, ids);
	}

	@PostMapping(value = "/clear")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="清空购物车",httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
	public void clear(HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		cartItemService.clear(userId);
	}

	private Long getCurrentUserId(HttpServletRequest request) {
		SessionInfo session = CacheUtil.getSessionInfo(request);
		return session.getId();
	}
}
