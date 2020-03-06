package me.izhong.jobs.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopAd;

@Controller
@RequestMapping("/ext/shop/ad")
public class ShopAdController {

	private String prefix = "ext/shop/ad";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String ad() {
		return prefix + "/ad";
	}

    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopAd> list(
    		HttpServletRequest request,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "content", required = false) String content) {
		PageModel<ShopAd> page = shopServiceReference.adService.pageList(PageRequestUtil.fromRequest(request), name, content);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopAd shopAd) {
    	shopServiceReference.adService.create(shopAd);
    }

    @GetMapping("/edit/{adId}")
    public String edit(@PathVariable("adId") Long adId, ModelMap model) {
		if (adId == null) {
			throw BusinessException.build("adId不能为空");
		}
		ShopAd shopAd = shopServiceReference.adService.find(adId);
		if (shopAd == null) {
			throw BusinessException.build(String.format("广告不存在%s", adId));
		}
		model.addAttribute("ad", shopAd);
		return prefix + "/edit";
    }

    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopAd shopAd) {
		shopServiceReference.adService.edit(shopAd);
    }

	@PostMapping("/edit/showStatus")
	@AjaxWrapper
	public void updateRecommendStatus(@RequestParam("ids") List<Long> ids, @RequestParam("showStatus") Integer showStatus) {
		shopServiceReference.adService.updateShowStatus(ids, showStatus);
	}

    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.adService.remove(ids);
    	if (!result) {
    		throw BusinessException.build("删除失败");
    	}
    }
}
