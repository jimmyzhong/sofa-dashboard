package me.izhong.shop.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.dto.GoodsCollectionParam;
import me.izhong.shop.service.ICollectionService;

@Controller
@RequestMapping(value = "/api/collect")
public class UserCollectionController {

	@Autowired
	private ICollectionService collectionService;

	/**
	 * 
	 * @param param
	 */
	@PostMapping(value = "/add")
    @ResponseBody
	public void add(@RequestBody GoodsCollectionParam param, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		param.setUserId(userId);
		collectionService.add(param);
	}

	/**
	 * 
	 * @param productId
	 */
	@PostMapping(value = "/delete")
    @ResponseBody
	public void delete(@RequestParam Long productId, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		collectionService.delete(userId, productId);
	}

	/**
	 * 
	 */
	@GetMapping(value = "/list")
    @ResponseBody
	public List<GoodsCollectionParam> list(HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		return collectionService.list(userId);
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
