package me.izhong.jobs.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopOrder;

@Controller
@RequestMapping("/ext/shop/order")
public class ShopOrderController {

	private String prefix = "ext/shop/order";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String goods() {
		return prefix + "/order";
	}

	@GetMapping("/view")
	public ShopOrder view(Long orderId) {
		return null;
	}

	@PostMapping("/list")
	@AjaxWrapper
	public PageModel<ShopOrder> pageList(HttpServletRequest request, ShopOrder order) {
		PageModel<ShopOrder> page = shopServiceReference.orderService.pageList(PageRequestUtil.fromRequest(request), order);
		return page;
	}

	@PostMapping("/edit/orderStatus")
	@AjaxWrapper
	public void updatePublishStatus(@RequestParam("ids") List<Long> ids, @RequestParam("orderStatus") Integer orderStatus) {
		shopServiceReference.orderService.updateOrderStatus(ids, orderStatus);
	}
}
