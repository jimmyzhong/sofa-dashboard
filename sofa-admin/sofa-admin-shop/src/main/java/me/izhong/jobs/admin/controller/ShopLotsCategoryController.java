package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopLotsCategory;

@Controller
@RequestMapping("/ext/shop/lotsCategory")
public class ShopLotsCategoryController {

	private String prefix = "ext/shop/lotsCategory";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String ad() {
		return prefix + "/lotsCategory";
	}

	@RequiresPermissions(ShopPermissions.LotsCategory.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopLotsCategory> list(
    		HttpServletRequest request,
			@RequestParam(value = "name", required = false) String name) {
		PageModel<ShopLotsCategory> page = shopServiceReference.lotsCategoryService.pageList(PageRequestUtil.fromRequest(request), name);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.LotsCategory.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopLotsCategory shopLotsCategory) {
    	checkField(shopLotsCategory.getName(), "分区名称");
    	checkField(shopLotsCategory.getLogo(), "分区图片");
    	checkField(shopLotsCategory.getPassword(), "分区密码");
    	checkField(shopLotsCategory.getAdmin(), "分区管理员");
    	if (shopLotsCategory.getSort() == null) {
    		shopLotsCategory.setSort(0);
    	}
    	shopServiceReference.lotsCategoryService.create(shopLotsCategory);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, ModelMap model) {
		ShopLotsCategory shopLotsCategory = shopServiceReference.lotsCategoryService.find(id);
		if (shopLotsCategory == null) {
			throw BusinessException.build(String.format("拍卖分区不存在%s", id));
		}
		model.addAttribute("lotsCategory", shopLotsCategory);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.LotsCategory.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopLotsCategory shopLotsCategory) {
    	checkField(shopLotsCategory.getName(), "分区名称");
    	checkField(shopLotsCategory.getLogo(), "分区图片");
    	checkField(shopLotsCategory.getPassword(), "分区密码");
    	checkField(shopLotsCategory.getAdmin(), "分区管理员");
		shopServiceReference.lotsCategoryService.edit(shopLotsCategory);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Integer id, Model model) {
		ShopLotsCategory shopLotsCategory = shopServiceReference.lotsCategoryService.find(id);
		if (shopLotsCategory == null) {
			throw BusinessException.build(String.format("拍卖分区不存在%s", id));
		}
		model.addAttribute("lotsCategory", shopLotsCategory);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.LotsCategory.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.lotsCategoryService.remove(ids);
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
