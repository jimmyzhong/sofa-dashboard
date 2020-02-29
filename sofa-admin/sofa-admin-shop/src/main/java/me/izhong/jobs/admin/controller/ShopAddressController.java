package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import me.izhong.db.mongo.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopReceiveAddress;

@Controller
@RequestMapping("/ext/shop/receive/address")
public class ShopAddressController {

	private String prefix = "ext/shop/receive/address";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String address() {
		return prefix + "/receive/address";
	}

	@PostMapping("/list")
    @AjaxWrapper
	public PageModel<ShopReceiveAddress> pageList(
			HttpServletRequest request,
			@RequestParam(value = "phne", required = false) String phone,
			@RequestParam(value = "name", required = false) String name) {
		PageModel<ShopReceiveAddress> page = shopServiceReference.receiveAddressService.pageList(PageRequestUtil.fromRequest(request), phone, name);
		return page;
	}
}
