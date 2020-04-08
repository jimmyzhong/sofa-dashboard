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
import me.izhong.jobs.model.ShopSuppliers;

@Controller
@RequestMapping("/ext/shop/suppliers")
public class ShopSuppliersController {

	private String prefix = "ext/shop/suppliers";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String ad() {
		return prefix + "/suppliers";
	}

	@RequiresPermissions(ShopPermissions.Supplier.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopSuppliers> list(
    		HttpServletRequest request,
			@RequestParam(value = "name", required = false) String name) {
		PageModel<ShopSuppliers> page = shopServiceReference.suppliersService.pageList(PageRequestUtil.fromRequest(request), name);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.Supplier.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopSuppliers shopSuppliers) {
    	checkField(shopSuppliers.getName(), "供应商名称");
    	checkField(shopSuppliers.getLogo(), "供应商Logo");
    	checkField(shopSuppliers.getDescription(), "供应商简介");
    	checkField(shopSuppliers.getContent(), "供应商详情");
    	shopServiceReference.suppliersService.create(shopSuppliers);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopSuppliers shopSuppliers = shopServiceReference.suppliersService.find(id);
		if (shopSuppliers == null) {
			throw BusinessException.build(String.format("供应商不存在%s", id));
		}
		model.addAttribute("supplier", shopSuppliers);
		return prefix + "/edit";
    }

    @GetMapping("/goods/{id}")
    public String goods(@PathVariable("id") Long id, ModelMap model) {
		ShopSuppliers shopSuppliers = shopServiceReference.suppliersService.find(id);
		if (shopSuppliers == null) {
			throw BusinessException.build(String.format("供应商不存在%s", id));
		}
		model.addAttribute("id", id);
		return prefix + "/goods";
    }

	@RequiresPermissions(ShopPermissions.Supplier.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopSuppliers shopSuppliers) {
    	checkField(shopSuppliers.getName(), "供应商名称");
    	checkField(shopSuppliers.getLogo(), "供应商Logo");
    	checkField(shopSuppliers.getDescription(), "供应商简介");
    	checkField(shopSuppliers.getContent(), "供应商详情");
		shopServiceReference.suppliersService.edit(shopSuppliers);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopSuppliers shopSuppliers = shopServiceReference.suppliersService.find(id);
		if (shopSuppliers == null) {
			throw BusinessException.build(String.format("供应商不存在%s", id));
		}
		model.addAttribute("supplier", shopSuppliers);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.Supplier.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.suppliersService.remove(ids);
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
