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
import me.izhong.jobs.model.ShopConsignmentRule;

@Controller
@RequestMapping("/ext/shop/consignmentRule")
public class ShopConsignmentRuleController {

	private String prefix = "ext/shop/consignmentRule";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String consignmentRule() {
		return prefix + "/consignmentRule";
	}

	@RequiresPermissions(ShopPermissions.ConsignmentRule.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopConsignmentRule> list(HttpServletRequest request) {
		PageModel<ShopConsignmentRule> page = shopServiceReference.consignmentRuleService.pageList(PageRequestUtil.fromRequest(request));
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.ConsignmentRule.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopConsignmentRule shopConsignmentRule) {
//    	checkField(shopConsignmentRule.getBeginTime(), "开始时间");
//    	checkField(shopConsignmentRule.getEndTime(), "结束时间");
//    	checkField(shopConsignmentRule.getLimitRule(), "寄售规则");
    	checkField(shopConsignmentRule.getTimeStep(), "时间跨度");
    	checkField(shopConsignmentRule.getReduceValue(), "降价权重");
    	shopServiceReference.consignmentRuleService.create(shopConsignmentRule);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopConsignmentRule shopConsignmentRule = shopServiceReference.consignmentRuleService.find(id);
		if (shopConsignmentRule == null) {
			throw BusinessException.build(String.format("寄售规则不存在%s", id));
		}
		model.addAttribute("consignmentRule", shopConsignmentRule);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.ConsignmentRule.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopConsignmentRule shopConsignmentRule) {
//    	checkField(shopConsignmentRule.getBeginTime(), "开始时间");
//    	checkField(shopConsignmentRule.getEndTime(), "结束时间");
//    	checkField(shopConsignmentRule.getLimitRule(), "寄售规则");
		checkField(shopConsignmentRule.getTimeStep(), "时间跨度");
		checkField(shopConsignmentRule.getReduceValue(), "降价权重");
		shopServiceReference.consignmentRuleService.edit(shopConsignmentRule);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopConsignmentRule shopConsignmentRule = shopServiceReference.consignmentRuleService.find(id);
		if (shopConsignmentRule == null) {
			throw BusinessException.build(String.format("寄售规则不存在%s", id));
		}
		model.addAttribute("consignmentRule", shopConsignmentRule);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.ConsignmentRule.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public void remove(String ids) {
    	boolean result = shopServiceReference.consignmentRuleService.remove(ids);
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
