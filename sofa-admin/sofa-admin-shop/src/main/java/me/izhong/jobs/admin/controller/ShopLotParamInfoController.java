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
import me.izhong.jobs.model.ShopLotParamInfo;

@Controller
@RequestMapping("/ext/shop/lotParamInfo")
public class ShopLotParamInfoController {

	private String prefix = "ext/shop/lotParamInfo";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String lotParamInfo() {
		return prefix + "/lotParamInfo";
	}

	@RequiresPermissions(ShopPermissions.LotParamInfo.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<ShopLotParamInfo> list(HttpServletRequest request) {
		PageModel<ShopLotParamInfo> page = shopServiceReference.lotParamInfoService.pageList(PageRequestUtil.fromRequest(request));
		return page;
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.LotParamInfo.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void add(ShopLotParamInfo shopLotParamInfo) {
    	checkField(shopLotParamInfo.getType(), "奖励类型");
    	checkField(shopLotParamInfo.getRadix(), "奖励基数");
    	checkField(shopLotParamInfo.getBalanceReward(), "余额奖励");
    	checkField(shopLotParamInfo.getPointReward(), "积分奖励");
    	shopServiceReference.lotParamInfoService.create(shopLotParamInfo);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
		ShopLotParamInfo shopLotParamInfo = shopServiceReference.lotParamInfoService.find(id);
		if (shopLotParamInfo == null) {
			throw BusinessException.build(String.format("参数不存在%s", id));
		}
		model.addAttribute("lotParamInfo", shopLotParamInfo);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.LotParamInfo.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public void edit(ShopLotParamInfo shopLotParamInfo) {
    	checkField(shopLotParamInfo.getType(), "奖励类型");
    	checkField(shopLotParamInfo.getRadix(), "奖励基数");
    	checkField(shopLotParamInfo.getBalanceReward(), "余额奖励");
    	checkField(shopLotParamInfo.getPointReward(), "积分奖励");
		shopServiceReference.lotParamInfoService.edit(shopLotParamInfo);
    }

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		ShopLotParamInfo shopLotParamInfo = shopServiceReference.lotParamInfoService.find(id);
		if (shopLotParamInfo == null) {
			throw BusinessException.build(String.format("参数不存在%s", id));
		}
		model.addAttribute("lotParamInfo", shopLotParamInfo);
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
