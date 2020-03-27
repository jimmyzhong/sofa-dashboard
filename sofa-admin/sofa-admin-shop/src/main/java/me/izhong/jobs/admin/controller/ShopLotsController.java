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
import me.izhong.jobs.model.ShopLots;

@Controller
@RequestMapping("/ext/shop/lots")
public class ShopLotsController {

	private String prefix = "ext/shop/lots";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String lots() {
		return prefix + "/lots";
	}

	@RequiresPermissions(ShopPermissions.Lots.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopLots> list(
    		HttpServletRequest request) {
		PageModel<ShopLots> page = shopServiceReference.lotsService.pageList(PageRequestUtil.fromRequest(request));
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.Lots.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopLots shopLots) {
    	checkField(shopLots.getName(), "拍卖品名称");
    	checkField(shopLots.getStartPrice(), "起拍价");
    	checkField(shopLots.getAddPrice(), "加价幅度");
    	checkField(shopLots.getDeposit(), "保证金");
    	checkField(shopLots.getStartTime(), "开始时间");
    	checkField(shopLots.getEndTime(), "结束时间");
    	checkField(shopLots.getUserLevel(), "最低会员级别");
    	shopServiceReference.lotsService.create(shopLots);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopLots shopLots = shopServiceReference.lotsService.find(id);
		if (shopLots == null) {
			throw BusinessException.build(String.format("拍卖品不存在%s", id));
		}
		model.addAttribute("lots", shopLots);
		return prefix + "/edit";
    }
    @GetMapping("/goods")
    public String goods( ModelMap model) {
		return prefix + "/goods";
    }

	@RequiresPermissions(ShopPermissions.Lots.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopLots shopLots) {
    	checkField(shopLots.getName(), "拍卖品名称");
    	checkField(shopLots.getStartPrice(), "起拍价");
    	checkField(shopLots.getAddPrice(), "加价幅度");
    	checkField(shopLots.getDeposit(), "保证金");
    	checkField(shopLots.getStartTime(), "开始时间");
    	checkField(shopLots.getEndTime(), "结束时间");
    	checkField(shopLots.getUserLevel(), "最低会员级别");
		shopServiceReference.lotsService.edit(shopLots);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopLots shopLots = shopServiceReference.lotsService.find(id);
		if (shopLots == null) {
			throw BusinessException.build(String.format("拍卖品不存在%s", id));
		}
		model.addAttribute("lots", shopLots);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.Lots.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.lotsService.remove(ids);
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
