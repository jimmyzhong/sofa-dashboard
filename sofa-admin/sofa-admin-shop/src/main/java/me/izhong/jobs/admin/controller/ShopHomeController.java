package me.izhong.jobs.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.jobs.admin.service.ShopServiceReference;

@Controller
public class ShopHomeController {

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

    // 系统介绍
    @GetMapping("/system/main_job")
    public String main(ModelMap mmap) {
        mmap.put("version", "1.0");
        return "ext/shop/main_v1";
    }

	@RequestMapping("/home")
	@AjaxWrapper
    public Map<String, Object> home() {
    	Map<String, Object> map = new HashMap<String, Object>();
    	long registerNum = shopServiceReference.homeService.countUser();
    	long normalGoodsNum = shopServiceReference.homeService.countNormalGoods();
    	long consignmentGoodsNum = shopServiceReference.homeService.countConsignmentGoods();
    	map.put("registerNum", registerNum);
    	map.put("normalGoodsNum", normalGoodsNum);
    	map.put("consignmentGoodsNum", consignmentGoodsNum);
    	return map;
    }
}