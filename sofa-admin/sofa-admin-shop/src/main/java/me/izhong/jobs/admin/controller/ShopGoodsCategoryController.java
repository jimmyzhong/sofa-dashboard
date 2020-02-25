package me.izhong.jobs.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.Convert;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.jobs.admin.service.ShopServiceReference;
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

	@GetMapping("/view")
	public ShopGoodsCategory view(Long categoryId) {
		return shopServiceReference.goodsCategoryService.find(categoryId);
	}

	@PostMapping("/list")
	@AjaxWrapper
	public PageModel<ShopGoodsCategory> pageList(
			HttpServletRequest request,
			@RequestParam(value = "type", defaultValue = "0") Long type,
			@RequestParam(value = "name", required = false) String name) {
		PageModel<ShopGoodsCategory> page = shopServiceReference.goodsCategoryService.pageList(PageRequestUtil.fromRequest(request), type, name);
		return page;
	}

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @AjaxWrapper
    public void addGoodsCategory(ShopGoodsCategory goodsCategory) {
    	shopServiceReference.goodsCategoryService.create(goodsCategory);
    }

	@GetMapping("/edit/{categoryId}")
	public String edit(@PathVariable("categoryId") Long categoryId, Model model) {
		if (categoryId == null) {
			throw BusinessException.build("categoryId不能为空");
		}
		ShopGoodsCategory goodsCategory = shopServiceReference.goodsCategoryService.find(categoryId);
		if (goodsCategory == null) {
			throw BusinessException.build(String.format("商品类目不存在%s", categoryId));
		}
		model.addAttribute("goodsCategory", goodsCategory);
		return prefix + "/edit";
	}

	@PostMapping("/edit")
	@AjaxWrapper
	public void edit(ShopGoodsCategory goodsCategory) {
		ShopGoodsCategory obj = shopServiceReference.goodsCategoryService.find(goodsCategory.getId());
		if (obj == null) {
			throw BusinessException.build(String.format("商品类目不存在%s", goodsCategory.getId()));
		}
		shopServiceReference.goodsCategoryService.edit(obj);
	}

	@PostMapping("/edit/showStatus")
	@AjaxWrapper
	public void updatePublishStatus(@RequestParam("ids") List<Long> ids, @RequestParam("showStatus") Integer showStatus) {
		shopServiceReference.goodsCategoryService.updateShowStatus(ids, showStatus);
	}

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
