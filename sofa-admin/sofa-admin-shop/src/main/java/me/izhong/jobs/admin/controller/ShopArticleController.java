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
import me.izhong.jobs.model.ShopArticles;

@Controller
@RequestMapping("/ext/shop/article")
public class ShopArticleController {

	private String prefix = "ext/shop/article";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String ad() {
		return prefix + "/article";
	}

	@RequiresPermissions(ShopPermissions.Article.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopArticles> list(
    		HttpServletRequest request,
			@RequestParam(value = "title", required = false) String title) {
		PageModel<ShopArticles> page = shopServiceReference.articlesService.pageList(PageRequestUtil.fromRequest(request), title);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.Article.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopArticles shopArticles) {
    	checkField(shopArticles.getTitle(), "文章标题");
    	checkField(shopArticles.getContent(), "文章内容");
    	shopServiceReference.articlesService.create(shopArticles);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopArticles shopArticles = shopServiceReference.articlesService.find(id);
		if (shopArticles == null) {
			throw BusinessException.build(String.format("文章不存在%s", id));
		}
		model.addAttribute("articles", shopArticles);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.Article.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopArticles shopArticles) {
    	checkField(shopArticles.getTitle(), "文章标题");
    	checkField(shopArticles.getContent(), "文章内容");
		shopServiceReference.articlesService.edit(shopArticles);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopArticles shopArticles = shopServiceReference.articlesService.find(id);
		if (shopArticles == null) {
			throw BusinessException.build(String.format("文章不存在%s", id));
		}
		model.addAttribute("articles", shopArticles);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.Article.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.articlesService.remove(ids);
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
