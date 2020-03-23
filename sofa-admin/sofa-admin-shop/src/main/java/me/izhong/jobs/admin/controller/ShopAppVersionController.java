package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import me.izhong.jobs.model.ShopAppVersions;

@Controller
@RequestMapping("/ext/shop/version")
public class ShopAppVersionController {

	private String prefix = "ext/shop/version";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String version() {
		return prefix + "/version";
	}

	@RequiresPermissions(ShopPermissions.Version.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopAppVersions> list(
    		HttpServletRequest request,
			@RequestParam(value = "type", required = false) String type) {
		PageModel<ShopAppVersions> page = shopServiceReference.appVersionService.pageList(PageRequestUtil.fromRequest(request), type);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.Version.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopAppVersions shopAppVersion) {
    	checkField(shopAppVersion.getVersion(), "版本号");
    	checkField(shopAppVersion.getDescription(), "版本内容");
    	checkField(shopAppVersion.getType(), "版本类型");
		checkField(shopAppVersion.getUrl(), "版本链接");
    	shopServiceReference.appVersionService.create(shopAppVersion);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopAppVersions shopVersion = shopServiceReference.appVersionService.find(id);
		if (shopVersion == null) {
			throw BusinessException.build(String.format("版本信息不存在%s", id));
		}
		model.addAttribute("version", shopVersion);
		return prefix + "/edit";
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, ModelMap model) {
		ShopAppVersions shopVersion = shopServiceReference.appVersionService.find(id);
		if (shopVersion == null) {
			throw BusinessException.build(String.format("版本信息不存在%s", id));
		}
		model.addAttribute("version", shopVersion);
		return prefix + "/detail";
	}


	@RequiresPermissions(ShopPermissions.Version.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopAppVersions shopAppVersion) {
    	checkField(shopAppVersion.getVersion(), "版本号");
    	checkField(shopAppVersion.getDescription(), "版本内容");
    	checkField(shopAppVersion.getType(), "版本类型");
		checkField(shopAppVersion.getUrl(), "版本链接");
		shopServiceReference.appVersionService.edit(shopAppVersion);
    }

	@RequiresPermissions(ShopPermissions.Version.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.appVersionService.remove(ids);
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
