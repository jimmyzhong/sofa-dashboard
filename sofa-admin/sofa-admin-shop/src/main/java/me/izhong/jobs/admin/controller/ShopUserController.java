package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
