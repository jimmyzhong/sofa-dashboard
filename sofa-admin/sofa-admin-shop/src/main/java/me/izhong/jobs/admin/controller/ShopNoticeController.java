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
import me.izhong.jobs.model.ShopNotice;

@Controller
@RequestMapping("/ext/shop/notice")
public class ShopNoticeController {

	private String prefix = "ext/shop/notice";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String ad() {
		return prefix + "/notice";
	}

    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopNotice> list(
    		HttpServletRequest request,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "status", required = false) Integer status) {
		PageModel<ShopNotice> page = shopServiceReference.noticeService.pageList(PageRequestUtil.fromRequest(request), title, status);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopNotice shopNotice) {
    	checkField(shopNotice.getTitle(), "公告标题");
    	checkField(shopNotice.getContent(), "公告内容");
    	checkField(shopNotice.getStatus(), "公告状态");
    	if (shopNotice.getIsTop() == null) {
    		shopNotice.setIsTop(0);
    	}
    	shopServiceReference.noticeService.create(shopNotice);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
    	ShopNotice shopNotice = shopServiceReference.noticeService.find(id);
		if (shopNotice == null) {
			throw BusinessException.build(String.format("公告不存在%s", id));
		}
		model.addAttribute("notice", shopNotice);
		return prefix + "/edit";
    }

    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopNotice shopNotice) {
    	checkField(shopNotice.getTitle(), "公告标题");
    	checkField(shopNotice.getContent(), "公告内容");
    	checkField(shopNotice.getStatus(), "公告状态");
    	if (shopNotice.getIsTop() == null) {
    		shopNotice.setIsTop(0);
    	}
		shopServiceReference.noticeService.edit(shopNotice);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopNotice shopNotice = shopServiceReference.noticeService.find(id);
		if (shopNotice == null) {
			throw BusinessException.build(String.format("公告不存在%s", id));
		}
		model.addAttribute("notice", shopNotice);
		return prefix + "/detail";
	}

    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.noticeService.remove(ids);
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
