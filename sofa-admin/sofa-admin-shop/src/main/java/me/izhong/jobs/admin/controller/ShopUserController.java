package me.izhong.jobs.admin.controller;

import me.izhong.common.util.Convert;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.jobs.model.ShopUser;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/ext/shop/user")
public class ShopUserController {

	private String prefix = "ext/shop/user";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String user(Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {
		return prefix + "/page";
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/view")
	@AjaxWrapper
	public ShopUser view(Long jobId) {
		return shopServiceReference.userService.find(jobId);
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopUser> pageList(HttpServletRequest request, ShopUser ino) {
		PageModel<ShopUser>  pm = shopServiceReference.userService.pageList(PageRequestUtil.fromRequest(request),ino);
		return pm;
	}

	@GetMapping("/edit/{userId}")
	public String edit(@PathVariable("jobId") Long userId,Model model) {
		if(userId == null){
			throw BusinessException.build("userId 不能为空");
		}
		ShopUser u = shopServiceReference.userService.find(userId);
		if(u == null) {
			throw BusinessException.build(String.format("用户不存在%s",userId));
		}
		model.addAttribute("user",u);
		return prefix + "/edit";
	}

	@Log(title = "APP用户", businessType = BusinessType.DELETE)
	@RequiresPermissions(ShopPermissions.User.REMOVE)
	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		Long[] jobIds = Convert.toLongArray(ids);
		for(Long jobId : jobIds) {
			boolean rt = shopServiceReference.userService.remove(jobId);
			if (!rt) {
				throw BusinessException.build("删除失败");
			}
		}
	}

}
