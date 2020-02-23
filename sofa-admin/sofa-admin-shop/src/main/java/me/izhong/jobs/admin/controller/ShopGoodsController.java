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
import me.izhong.common.util.Convert;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopGoods;

@Controller
@RequestMapping("/ext/shop/goods")
public class ShopGoodsController {

	private String prefix = "ext/shop/goods";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String goods() {
		return prefix + "/goods";
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/view")
	@AjaxWrapper
	public ShopGoods view(Long goodsId) {
		return shopServiceReference.goodsService.find(goodsId);
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopGoods> pageList(HttpServletRequest request, ShopGoods goods) {
		PageModel<ShopGoods> pm = shopServiceReference.goodsService.pageList(PageRequestUtil.fromRequest(request), goods);
		return pm;
	}

	@GetMapping("/edit/{goodsId}")
	public String edit(@PathVariable("goodsId") Long goodsId, Model model) {
		if (goodsId == null) {
			throw BusinessException.build("goodsId不能为空");
		}
		ShopGoods goods = shopServiceReference.goodsService.find(goodsId);
		if (goods == null) {
			throw BusinessException.build(String.format("商品不存在%s", goodsId));
		}
		model.addAttribute("goods", goods);
		return prefix + "/edit";
	}

	@Log(title = "商品管理", businessType = BusinessType.UPDATE)
	@RequiresPermissions(ShopPermissions.User.EDIT)
	@PostMapping("/edit")
	@AjaxWrapper
	public void edit(ShopGoods goods) {
		ShopGoods obj = shopServiceReference.goodsService.find(goods.getId());
		if (obj == null) {
			throw BusinessException.build(String.format("商品不存在%s", goods.getId()));
		}
		shopServiceReference.goodsService.edit(obj);
	}

	@Log(title = "商品管理", businessType = BusinessType.DELETE)
	@RequiresPermissions(ShopPermissions.User.REMOVE)
	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		Long[] goodsIds = Convert.toLongArray(ids);
		for (Long goodsId : goodsIds) {
			boolean rt = shopServiceReference.goodsService.remove(goodsId);
			if (!rt) {
				throw BusinessException.build("删除失败");
			}
		}
	}
}
