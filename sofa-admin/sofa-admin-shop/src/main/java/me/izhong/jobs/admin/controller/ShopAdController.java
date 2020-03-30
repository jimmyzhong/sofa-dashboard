package me.izhong.jobs.admin.controller;

import java.util.List;

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

	@RequiresPermissions(ShopPermissions.Ad.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopAd> list(
    		HttpServletRequest request,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "status", required = false) Integer status) {
		PageModel<ShopAd> page = shopServiceReference.adService.pageList(PageRequestUtil.fromRequest(request), name, status);
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.Ad.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopAd shopAd) {
    	checkField(shopAd.getAdName(), "广告名称");
    	checkField(shopAd.getAdLink(), "广告链接");
    	checkField(shopAd.getImageUrl(), "广告图片");
    	if (shopAd.getStatus() == null) {
    		shopAd.setStatus(1);
    	}
    	if (shopAd.getSort() == null) {
    		shopAd.setSort(0);
    	}
    	shopServiceReference.adService.create(shopAd);
    }

    @GetMapping("/edit/{adId}")
    public String edit(@PathVariable("adId") Long adId, ModelMap model) {
		ShopAd shopAd = shopServiceReference.adService.find(adId);
		if (shopAd == null) {
			throw BusinessException.build(String.format("广告不存在%s", adId));
		}
		model.addAttribute("ad", shopAd);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.Ad.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopAd shopAd) {
    	checkField(shopAd.getAdName(), "广告名称");
    	checkField(shopAd.getAdLink(), "广告链接");
    	checkField(shopAd.getImageUrl(), "广告图片");
    	if (shopAd.getStatus() == null) {
    		shopAd.setStatus(1);
    	}
    	if (shopAd.getSort() == null) {
    		shopAd.setSort(0);
    	}
		shopServiceReference.adService.edit(shopAd);
    }

	@RequiresPermissions(ShopPermissions.Ad.EDIT)
	@PostMapping("/edit/showStatus")
	@AjaxWrapper
	public void updateRecommendStatus(@RequestParam("ids") List<Long> ids, @RequestParam("showStatus") Integer showStatus) {
		shopServiceReference.adService.updateShowStatus(ids, showStatus);
	}

	@GetMapping("/detail/{adId}")
	public String detail(@PathVariable("adId") Long adId, Model model) {
		ShopAd shopAd = shopServiceReference.adService.find(adId);
		if (shopAd == null) {
			throw BusinessException.build(String.format("广告不存在%s", adId));
		}
		model.addAttribute("ad", shopAd);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.Ad.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.adService.remove(ids);
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
