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
import me.izhong.jobs.model.ShopPlatformParamConfig;

@Controller
@RequestMapping("/ext/shop/platformParamConfig")
public class ShopPlatformParamConfigController {

	private String prefix = "ext/shop/platformParamConfig";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String ad() {
		return prefix + "/platformParamConfig";
	}

	@RequiresPermissions(ShopPermissions.PlatformParamConfig.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopPlatformParamConfig> list(HttpServletRequest request) {
		PageModel<ShopPlatformParamConfig> page = shopServiceReference.platformParamConfigService.pageList(PageRequestUtil.fromRequest(request));
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.PlatformParamConfig.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopPlatformParamConfig config) {
    	checkField(config.getConfigKey(), "参数名");
    	checkField(config.getConfigValue(), "参数值");
    	shopServiceReference.platformParamConfigService.create(config);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopPlatformParamConfig shopConfig = shopServiceReference.platformParamConfigService.find(id);
		if (shopConfig == null) {
			throw BusinessException.build(String.format("平台参数不存在%s", id));
		}
		model.addAttribute("platformParamConfig", shopConfig);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.PlatformParamConfig.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopPlatformParamConfig config) {
    	checkField(config.getConfigKey(), "参数名");
    	checkField(config.getConfigValue(), "参数值");
		shopServiceReference.platformParamConfigService.edit(config);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopPlatformParamConfig shopConfig = shopServiceReference.platformParamConfigService.find(id);
		if (shopConfig == null) {
			throw BusinessException.build(String.format("平台参数不存在%s", id));
		}
		model.addAttribute("platformParamConfig", shopConfig);
		return prefix + "/detail";
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
