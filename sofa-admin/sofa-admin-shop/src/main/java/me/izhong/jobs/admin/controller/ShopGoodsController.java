package me.izhong.jobs.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.Convert;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopGoods;

@Controller
@RequestMapping("/ext/shop/goods")
public class ShopGoodsController {

	private String prefix = "ext/shop/goods";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String goods() {
		return prefix + "/goods";
	}

	@PostMapping("/list")
	@AjaxWrapper
	public PageModel<ShopGoods> pageList(HttpServletRequest request, ShopGoods goods) {
		PageModel<ShopGoods> page = shopServiceReference.goodsService.pageList(PageRequestUtil.fromRequest(request), goods);
		return page;
	}

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @AjaxWrapper
    public void addGoods(ShopGoods goods) {
    	shopServiceReference.goodsService.create(goods);
    }

	@GetMapping("/edit/{goodsId}")
	public String edit(@PathVariable("goodsId") Long goodsId, Model model) {
		if (goodsId == null) {
			throw BusinessException.build("goodsId不能为空");
		}
		ShopGoods goods = shopServiceReference.goodsService.find(goodsId);
		if (goods == null) {
			throw BusinessException.build(String.format("商品不存在%s", goodsId));
		}
		model.addAttribute("goods", goods);
		return prefix + "/edit";
	}

	@PostMapping("/edit")
	@AjaxWrapper
	public void edit(ShopGoods goods) {
		ShopGoods obj = shopServiceReference.goodsService.find(goods.getId());
		if (obj == null) {
			throw BusinessException.build(String.format("商品不存在%s", goods.getId()));
		}
		shopServiceReference.goodsService.edit(obj);
	}

	@PostMapping("/edit/publishStatus")
	@AjaxWrapper
	public void updatePublishStatus(@RequestParam("ids") List<Long> ids, @RequestParam("publishStatus") Integer publishStatus) {
		shopServiceReference.goodsService.updatePublishStatus(ids, publishStatus);
	}

	@PostMapping("/edit/recommendStatus")
	@AjaxWrapper
	public void updateRecommendStatus(@RequestParam("ids") List<Long> ids, @RequestParam("recommendStatus") Integer recommendStatus) {
		shopServiceReference.goodsService.updateRecommendStatus(ids, recommendStatus);
	}

	@PostMapping("/edit/deleteStatus")
	@AjaxWrapper
	public void updateDeleteStatus(@RequestParam("ids") List<Long> ids, @RequestParam("deleteStatus") Integer deleteStatus) {
		shopServiceReference.goodsService.updateDeleteStatus(ids, deleteStatus);
	}

	@PostMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		Long[] goodsIds = Convert.toLongArray(ids);
		for (Long goodsId : goodsIds) {
			boolean rt = shopServiceReference.goodsService.remove(goodsId);
			if (!rt) {
				throw BusinessException.build("删除失败");
			}
		}
	}
}
