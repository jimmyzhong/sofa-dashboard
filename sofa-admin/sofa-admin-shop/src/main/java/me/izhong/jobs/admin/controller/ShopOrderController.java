package me.izhong.jobs.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.dto.OrderDeliveryParam;
import me.izhong.jobs.dto.OrderQueryParam;
import me.izhong.jobs.dto.ReceiverInfoParam;
import me.izhong.jobs.model.ShopOrder;

@Slf4j
@Controller
@RequestMapping("/ext/shop/order")
public class ShopOrderController {

	private String prefix = "ext/shop/order";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String order() {
		return prefix + "/order";
	}
	//寄售商品订单列表
	@GetMapping("/consignment")
	public String consignment(Model model) {
		return prefix + "/consignmentOrder";
	}
	//拍卖订单列表
	@GetMapping("/lots")
	public String lotsOrder(Model model) {
		return prefix + "/lotsOrder";
	}

	/**
	 * 查询订单
	 * @param request
	 * @param param
	 * @return
	 */
	@RequiresPermissions(ShopPermissions.Order.VIEW)
	@PostMapping("/list")
	@AjaxWrapper
	public PageModel<ShopOrder> pageList(HttpServletRequest request, OrderQueryParam param) {
		log.info("query order param:{}", param.toString());
		PageModel<ShopOrder> page = shopServiceReference.orderService.pageList(PageRequestUtil.fromRequest(request), param);
		return page;
	}

	/**
	 * 批量发货
	 * @param deliveryParamList
	 */
	
	@PostMapping("/update/delivery")
	@AjaxWrapper
	public void delivery(@RequestBody List<OrderDeliveryParam> deliveryParamList) {
		shopServiceReference.orderService.delivery(deliveryParamList);
	}

	/**
	 * 批量关闭订单
	 * @param ids
	 * @param note
	 */
	@PostMapping("/update/close")
	@AjaxWrapper
	public void close(@RequestParam("ids") List<Long> ids) {
		String note = "关闭订单";
		shopServiceReference.orderService.close(ids, note);
	}

	/**
	 * 批量删除订单
	 * @param ids
	 */
	@PostMapping("/delete")
	@AjaxWrapper
	public void delete(@RequestParam("ids") List<Long> ids) {
		shopServiceReference.orderService.delete(ids);
	}



	/**
	 * 订单详情、商品信息
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/detail/{id}")
	public String detail(@PathVariable Long id, Model model) {
		ShopOrder order = shopServiceReference.orderService.detail(id);
		model.addAttribute("order", order);
		return prefix + "/detail";
	}

	/**
	 * 修改收货人信息
	 * @param receiverInfoParam
	 */
	@PostMapping("/update/receiverInfo")
	@AjaxWrapper
	public void updateReceiverInfo(@RequestBody ReceiverInfoParam receiverInfoParam) {
		shopServiceReference.orderService.updateReceiverInfo(receiverInfoParam);
	}

	/**
	 * 订单备注
	 * @param id
	 * @param note
	 * @param status
	 */
	@PostMapping("/update/note")
	@AjaxWrapper
	public void updateNote(@RequestParam("id") Long id, @RequestParam("note") String note, @RequestParam("status") Integer status) {
		shopServiceReference.orderService.updateNote(id, note, status);
	}
}
