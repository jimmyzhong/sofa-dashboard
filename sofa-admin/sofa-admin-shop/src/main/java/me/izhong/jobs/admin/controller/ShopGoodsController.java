package me.izhong.jobs.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.Convert;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopGoods;

@Controller
@RequestMapping("/ext/shop/goods")
public class ShopGoodsController {

	private String prefix = "ext/shop/goods";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String goods(Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {
		return prefix + "/page";
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/view")
	@AjaxWrapper
	public ShopGoods view(Long jobId) {
		return shopServiceReference.goodsService.find(jobId);
	}

	@RequiresPermissions(ShopPermissions.User.VIEW)
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<ShopGoods> pageList(HttpServletRequest request, ShopGoods goods) {
		PageModel<ShopGoods> pm = shopServiceReference.goodsService.pageList(PageRequestUtil.fromRequest(request), goods);
		return pm;
	}

	@GetMapping("/edit/{goodsId}")
	public String edit(@PathVariable("jobId") Long goodsId, Model model) {
		if (goodsId == null) {
			throw BusinessException.build("goodsId不能为空");
		}
		ShopGoods good = shopServiceReference.goodsService.find(goodsId);
		if (good == null) {
			throw BusinessException.build(String.format("商品不存在%s", goodsId));
		}
		model.addAttribute("goods", good);
		return prefix + "/edit";
	}

	@Log(title = "商品管理", businessType = BusinessType.DELETE)
	@RequiresPermissions(ShopPermissions.User.REMOVE)
	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		Long[] jobIds = Convert.toLongArray(ids);
		for (Long jobId : jobIds) {
			boolean rt = shopServiceReference.goodsService.remove(jobId);
			if (!rt) {
				throw BusinessException.build("删除失败");
			}
		}
	}
}
