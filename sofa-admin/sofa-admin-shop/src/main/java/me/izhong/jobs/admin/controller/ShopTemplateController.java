package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

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
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopTemplate;

@Controller
@RequestMapping("/ext/shop/template")
public class ShopTemplateController {

	private String prefix = "ext/shop/template";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String ad() {
		return prefix + "/template";
	}

    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopTemplate> list(
    		HttpServletRequest request,
			@RequestParam(value = "title", required = false) String title) {
		PageModel<ShopTemplate> page = shopServiceReference.templateService.pageList(PageRequestUtil.fromRequest(request), title);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopTemplate shopTemplate) {
    	checkField(shopTemplate.getTitle(), "模板标题");
    	checkField(shopTemplate.getContent(), "模板内容");
    	if (shopTemplate.getType() == null) {
    		shopTemplate.setType(1);
    	}
    	shopServiceReference.templateService.create(shopTemplate);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
    	ShopTemplate shopTemplate = shopServiceReference.templateService.find(id);
		if (shopTemplate == null) {
			throw BusinessException.build(String.format("模板不存在%s", id));
		}
		model.addAttribute("template", shopTemplate);
		return prefix + "/edit";
    }

    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopTemplate shopTemplate) {
    	checkField(shopTemplate.getTitle(), "模板标题");
    	checkField(shopTemplate.getContent(), "模板内容");
		shopServiceReference.templateService.edit(shopTemplate);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopTemplate shopTemplate = shopServiceReference.templateService.find(id);
		if (shopTemplate == null) {
			throw BusinessException.build(String.format("模板不存在%s", id));
		}
		model.addAttribute("template", shopTemplate);
		return prefix + "/detail";
	}

	@PostMapping("/detail/{id}")
	@AjaxWrapper
	public ShopTemplate detailAjax(@PathVariable("id") Long id, Model model) {
		ShopTemplate shopTemplate = shopServiceReference.templateService.find(id);
		if (shopTemplate == null) {
			throw BusinessException.build(String.format("模板不存在%s", id));
		}
		return shopTemplate;
	}

    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.templateService.remove(ids);
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
