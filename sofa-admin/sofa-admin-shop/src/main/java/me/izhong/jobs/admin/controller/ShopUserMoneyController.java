package me.izhong.jobs.admin.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopPayRecord;

@Controller
@RequestMapping("/ext/shop/user/money")
public class ShopUserMoneyController {

	private String prefix = "ext/shop/user/money";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;


	@GetMapping
	public String user() {
		return prefix + "/user/money";
	}

	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopPayRecord> pageList(
			HttpServletRequest request,
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestParam(value = "moneyType", required = false) Integer moneyType) {
		if (userId == null) {
			return PageModel.instance(0L, new ArrayList<>());
		}
		List<Integer> moneyTypes = new LinkedList<>();
		if(null !=moneyType){
			moneyTypes.add(moneyType);
		}
		PageModel<ShopPayRecord> page = shopServiceReference.payRecordService.pageMoneyList(PageRequestUtil.fromRequest(request), userId, moneyTypes);
		return page;
	}

}