package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import me.izhong.jobs.model.ShopVersions;

@Controller
@RequestMapping("/ext/shop/version")
public class ShopVersionController {

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
    public PageModel<ShopVersions> list(
    		HttpServletRequest request,
			@RequestParam(value = "type", required = false) String type) {
		PageModel<ShopVersions> page = shopServiceReference.versionService.pageList(PageRequestUtil.fromRequest(request), type);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.Version.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopVersions shopVersion) {
    	checkField(shopVersion.getVersion(), "版本号");
    	checkField(shopVersion.getDesc(), "版本内容");
    	checkField(shopVersion.getType(), "版本类型");
    	if (!StringUtils.equals(shopVersion.getType(), "IOS")) {
    		checkField(shopVersion.getUrl(), "版本链接");
    	}
    	shopServiceReference.versionService.create(shopVersion);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopVersions shopVersion = shopServiceReference.versionService.find(id);
		if (shopVersion == null) {
			throw BusinessException.build(String.format("版本信息不存在%s", id));
		}
		model.addAttribute("version", shopVersion);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.Version.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopVersions shopVersion) {
    	checkField(shopVersion.getVersion(), "版本号");
    	checkField(shopVersion.getDesc(), "版本内容");
    	checkField(shopVersion.getType(), "版本类型");
    	if (!StringUtils.equals(shopVersion.getType(), "IOS")) {
    		checkField(shopVersion.getUrl(), "版本链接");
    	}
		shopServiceReference.versionService.edit(shopVersion);
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
