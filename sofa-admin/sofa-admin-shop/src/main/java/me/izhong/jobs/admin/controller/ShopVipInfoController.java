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

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopVipInfo;

@Controller
@RequestMapping("/ext/shop/vipInfo")
public class ShopVipInfoController {

	private String prefix = "ext/shop/vipInfo";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String vipInfo() {
		return prefix + "/vipInfo";
	}

	@RequiresPermissions(ShopPermissions.VipInfo.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopVipInfo> list(HttpServletRequest request) {
		PageModel<ShopVipInfo> page = shopServiceReference.vipInfoService.pageList(PageRequestUtil.fromRequest(request));
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.VipInfo.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopVipInfo shopVipInfo) {
    	checkField(shopVipInfo.getLevel(), "等级编号");
    	checkField(shopVipInfo.getName(), "等级名称");
    	shopServiceReference.vipInfoService.create(shopVipInfo);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopVipInfo shopVipInfo = shopServiceReference.vipInfoService.find(id);
		if (shopVipInfo == null) {
			throw BusinessException.build(String.format("VIP等级不存在%s", id));
		}
		model.addAttribute("vipInfo", shopVipInfo);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.VipInfo.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopVipInfo shopVipInfo) {
    	checkField(shopVipInfo.getLevel(), "等级编号");
    	checkField(shopVipInfo.getName(), "等级名称");
		shopServiceReference.vipInfoService.edit(shopVipInfo);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopVipInfo shopVipInfo = shopServiceReference.vipInfoService.find(id);
		if (shopVipInfo == null) {
			throw BusinessException.build(String.format("VIP等级不存在%s", id));
		}
		model.addAttribute("vipInfo", shopVipInfo);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.VipInfo.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.vipInfoService.remove(ids);
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
