package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopGoods;

@Controller
@RequestMapping("/ext/shop/consignment")
public class ShopConsignmentController {

	private String prefix = "ext/shop/consignment";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String consignment() {
		return prefix + "/consignment";
	}

	@RequiresPermissions(ShopPermissions.Consignment.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopGoods> list(HttpServletRequest request, ShopGoods shopGoods) {
		PageModel<ShopGoods> page = shopServiceReference.goodsService.pageConsignmentList(PageRequestUtil.fromRequest(request), shopGoods);
		return page;
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopGoods shopGoods = shopServiceReference.goodsService.find(id);
		if (shopGoods == null) {
			throw BusinessException.build(String.format("寄售商品不存在%s", id));
		}
		model.addAttribute("consignment", shopGoods);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.Consignment.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.goodsService.remove(ids);
    	if (!result) {
    		throw BusinessException.build("删除失败");
    	}
    }

    /**
     * 
     * @param field
     * @param message
     */
    public void checkField(Object field, String message) {
    	if (field == null) {
    		throw BusinessException.build(String.format("%s不能为空", message));
    	}
    }
}
