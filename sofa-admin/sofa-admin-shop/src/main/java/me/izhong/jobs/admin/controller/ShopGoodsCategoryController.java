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
import me.izhong.jobs.model.ShopGoodsCategory;

@Controller
@RequestMapping("/ext/shop/goods/category")
public class ShopGoodsCategoryController {

	private String prefix = "ext/shop/goods/category";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String goods() {
		return prefix + "/goods/category";
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/view")
	@AjaxWrapper
	public ShopGoodsCategory view(Long jobId) {
		return shopServiceReference.goodsCategoryService.find(jobId);
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopGoodsCategory> pageList(HttpServletRequest request, ShopGoodsCategory goodsCategory) {
		PageModel<ShopGoodsCategory> pm = shopServiceReference.goodsCategoryService.pageList(PageRequestUtil.fromRequest(request), goodsCategory);
		return pm;
	}

	@GetMapping("/edit/{categoryId}")
	public String edit(@PathVariable("jobId") Long categoryId, Model model) {
		if (categoryId == null) {
			throw BusinessException.build("goodsId不能为空");
		}
		ShopGoodsCategory goodsCategory = shopServiceReference.goodsCategoryService.find(categoryId);
		if (goodsCategory == null) {
			throw BusinessException.build(String.format("商品类目不存在%s", categoryId));
		}
		model.addAttribute("goodsCategory", goodsCategory);
		return prefix + "/edit";
	}

	@Log(title = "商品类目管理", businessType = BusinessType.UPDATE)
	@RequiresPermissions(ShopPermissions.User.EDIT)
	@PostMapping("/edit")
	@AjaxWrapper
	public void edit(ShopGoods goods) {
		ShopGoodsCategory obj = shopServiceReference.goodsCategoryService.find(goods.getId());
		if (obj == null) {
			throw BusinessException.build(String.format("商品类目不存在%s", goods.getId()));
		}
		shopServiceReference.goodsCategoryService.edit(obj);
	}

	@Log(title = "商品类目管理", businessType = BusinessType.DELETE)
	@RequiresPermissions(ShopPermissions.User.REMOVE)
	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		Long[] jobIds = Convert.toLongArray(ids);
		for (Long jobId : jobIds) {
			boolean rt = shopServiceReference.goodsCategoryService.remove(jobId);
			if (!rt) {
				throw BusinessException.build("删除失败");
			}
		}
	}
}
