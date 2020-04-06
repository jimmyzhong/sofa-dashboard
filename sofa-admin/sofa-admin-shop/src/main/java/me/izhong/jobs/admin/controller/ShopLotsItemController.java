package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopLotsItem;

@Controller
@RequestMapping("/ext/shop/lotsItem")
public class ShopLotsItemController {

	private String prefix = "ext/shop/lotsItem";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String user() {
		return prefix + "/lotsItem";
	}

	@RequiresPermissions(ShopPermissions.LotsItem.VIEW)
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopLotsItem> pageList(
			HttpServletRequest request,
			@RequestParam(value = "auctionId", required = false) Long auctionId) {
		PageModel<ShopLotsItem> page = shopServiceReference.lotsItemService.pageList(PageRequestUtil.fromRequest(request), auctionId);
		return page;
	}

}
