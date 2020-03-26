package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import me.izhong.jobs.dto.OrderQueryParam;
import me.izhong.jobs.model.ShopOrder;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.common.annotation.Log;
import me.izhong.dashboard.common.constants.BusinessType;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopUser;

@Controller
@RequestMapping("/ext/shop/user")
public class ShopUserController {

	private String prefix = "ext/shop/user";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String user() {
		return prefix + "/user";
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopUser> pageList(HttpServletRequest request, ShopUser shopUser) {
		PageModel<ShopUser> page = shopServiceReference.userService.pageList(PageRequestUtil.fromRequest(request), shopUser);
		return page;
	}

	@GetMapping("/edit/{userId}")
	public String edit(@PathVariable("userId") Long userId, Model model) {
		if (userId == null) {
			throw BusinessException.build("userId不能为空");
		}
		ShopUser user = shopServiceReference.userService.find(userId);
		if (user == null) {
			throw BusinessException.build(String.format("用户不存在%s", userId));
		}
		model.addAttribute("user", user);
		return prefix + "/edit";
	}

	@Log(title = "APP用户", businessType = BusinessType.UPDATE)
	@RequiresPermissions(ShopPermissions.User.EDIT)
	@PostMapping("/edit")
	@AjaxWrapper
	public void edit(ShopUser shopUser) {
		ShopUser user = shopServiceReference.userService.find(shopUser.getId());
		if (user == null) {
			throw BusinessException.build(String.format("用户不存在%s", shopUser.getId()));
		}
		ShopUser newUser = new ShopUser();
		newUser.setId(shopUser.getId());
		if (StringUtils.isNotBlank(shopUser.getLoginName())) {
			newUser.setLoginName(shopUser.getLoginName());
		}
		if (StringUtils.isNotBlank(shopUser.getNickName())) {
			newUser.setNickName(shopUser.getNickName());
		}
		if (StringUtils.isNotBlank(shopUser.getPassword())) {
			newUser.setPassword(shopUser.getPassword());
		}
		shopServiceReference.userService.edit(newUser);
	}

	//冻结用户
	@RequiresPermissions(ShopPermissions.User.EDIT)
	@PostMapping("/disable/{userId}")
	@AjaxWrapper
	public void disable(@PathVariable("userId") Long userId) {
		shopServiceReference.userService.disable(userId);
	}

	//解结用户
	@RequiresPermissions(ShopPermissions.User.EDIT)
	@PostMapping("/enable/{userId}")
	@AjaxWrapper
	public void enable(@PathVariable("userId") Long userId) {
		shopServiceReference.userService.enable(userId);
	}

	@GetMapping("/detail/{userId}")
	public String detail(@PathVariable("userId") Long userId, Model model) {
		ShopUser user = shopServiceReference.userService.find(userId);
		if (user == null) {
			throw BusinessException.build(String.format("用户不存在%s", userId));
		}
		ShopUser upperUser=new ShopUser();
		if(null!=user.getInviteUserId()){
			upperUser=shopServiceReference.userService.find(user.getInviteUserId());
		}
		model.addAttribute("upperUser", upperUser);
		model.addAttribute("user", user);
		return prefix + "/detail";
	}

	@Log(title = "APP用户", businessType = BusinessType.DELETE)
	@RequiresPermissions(ShopPermissions.User.REMOVE)
	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		boolean result = shopServiceReference.userService.remove(ids);
		if (!result) {
			throw BusinessException.build("删除失败");
		}
	}

	@GetMapping("/scoreDetail/{userId}")
	public String scoreDetail(@PathVariable("userId") Long userId, Model model) {
		model.addAttribute("userId", userId);
		return prefix + "/scoreDetail";
	}

	@GetMapping("/balanceDetail/{userId}")
	public String balanceDetail(@PathVariable("userId") Long userId, Model model) {
		model.addAttribute("userId", userId);
		return prefix + "/balanceDetail";
	}

	@GetMapping("/certification/{userId}")
	public String certification(@PathVariable("userId") Long userId, Model model) {
		ShopUser user = shopServiceReference.userService.find(userId);
		if (user == null) {
			throw BusinessException.build(String.format("用户不存在%s", userId));
		}
		model.addAttribute("user", user);
		return prefix + "/certification";
	}

	@GetMapping("/orderDetail/{userId}")
	public String orderDetail(@PathVariable("userId") Long userId, Model model) {
		model.addAttribute("userId", userId);
		return prefix + "/orderDetail";
	}

	@RequestMapping("/orderList")
	@AjaxWrapper
	public PageModel<ShopOrder> pageList(HttpServletRequest request, @RequestParam(value = "userId") Long userId, OrderQueryParam param) {
		PageModel<ShopOrder> page = shopServiceReference.orderService.pageList(PageRequestUtil.fromRequest(request),userId, param);
		return page;
	}
	//下级用户
	@RequestMapping("/inviteList")
	@AjaxWrapper
	public PageModel<ShopUser> inviteList(HttpServletRequest request, @RequestParam(value = "userId") Long userId, ShopUser shopUser) {
		shopUser.setInviteUserId(userId);
		PageModel<ShopUser> page = shopServiceReference.userService.pageList(PageRequestUtil.fromRequest(request), shopUser);
		return page;
	}
//	下下级用户
	@RequestMapping("/invite2List")
	@AjaxWrapper
	public PageModel<ShopUser> invite2List(HttpServletRequest request, @RequestParam(value = "userId") Long userId, ShopUser shopUser) {
		shopUser.setInviteUserId2(userId);
		PageModel<ShopUser> page = shopServiceReference.userService.pageList(PageRequestUtil.fromRequest(request), shopUser);
		return page;
	}
//	orderDetail
}
