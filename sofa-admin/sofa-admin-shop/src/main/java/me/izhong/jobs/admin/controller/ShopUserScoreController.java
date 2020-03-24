package me.izhong.jobs.admin.controller;

import java.util.ArrayList;

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
@RequestMapping("/ext/shop/user/score")
public class ShopUserScoreController {

	private String prefix = "ext/shop/user/score";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String user() {
		return prefix + "/user/score";
	}

	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopPayRecord> pageList(
			HttpServletRequest request,
			@RequestParam(value = "userId", required = false) Long userId) {
		if (userId == null) {
			return PageModel.instance(0L, new ArrayList<>());
		}
		PageModel<ShopPayRecord> page = shopServiceReference.payRecordService.pageScoreList(PageRequestUtil.fromRequest(request), userId);
		return page;
	}

}
