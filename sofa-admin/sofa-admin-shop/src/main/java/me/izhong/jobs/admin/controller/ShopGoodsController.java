package me.izhong.jobs.admin.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.config.ShopPermissions;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopGoods;

@Slf4j
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

	@RequiresPermissions(ShopPermissions.Goods.VIEW)
	@PostMapping("/list")
	@AjaxWrapper
	public PageModel<ShopGoods> pageList(HttpServletRequest request, ShopGoods goods) {
		goods.setProductType(0);//produceType为0 普通商品
		PageModel<ShopGoods> page = shopServiceReference.goodsService.pageList(PageRequestUtil.fromRequest(request), goods);
		return page;
	}

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

	@RequiresPermissions(ShopPermissions.Goods.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public void addGoods(ShopGoods goods) {
    	checkField(goods.getProductName(), "商品名称");
    	checkField(goods.getProductPic(), "商品封面图");
    	checkField(goods.getPrice(), "商品价格");
    	checkField(goods.getDetailDesc(), "商品详情");
    	if (goods.getPrice().compareTo(BigDecimal.ZERO) == 0) {
    		throw BusinessException.build("商品价格不能为0");
    	}
    	goods.setIsDelete(0);
    	shopServiceReference.goodsService.create(goods);
    }

	@GetMapping("/edit/{goodsId}")
	public String edit(@PathVariable("goodsId") Long goodsId, Model model) {
		ShopGoods goods = shopServiceReference.goodsService.find(goodsId);
		if (goods == null) {
			throw BusinessException.build(String.format("商品不存在%s", goodsId));
		}
		model.addAttribute("goods", goods);
		return prefix + "/edit";
	}

	@RequiresPermissions(ShopPermissions.Goods.EDIT)
	@PostMapping("/edit")
	@AjaxWrapper
	public void edit(ShopGoods goods) {
		ShopGoods obj = shopServiceReference.goodsService.find(goods.getId());
		if (obj == null) {
			throw BusinessException.build(String.format("商品不存在%s", goods.getId()));
		}
		log.info("edit goods => {}", goods);
		shopServiceReference.goodsService.edit(goods);
	}

	@RequiresPermissions(ShopPermissions.Goods.EDIT)
	@PostMapping("/edit/publishStatus")
	@AjaxWrapper
	public void updatePublishStatus(@RequestParam("ids") List<Long> ids, @RequestParam("publishStatus") Integer publishStatus) {
		shopServiceReference.goodsService.updatePublishStatus(ids, publishStatus);
	}

	@RequiresPermissions(ShopPermissions.Goods.EDIT)
	@PostMapping("/edit/recommendStatus")
	@AjaxWrapper
	public void updateRecommendStatus(@RequestParam("ids") List<Long> ids, @RequestParam("recommendStatus") Integer recommendStatus) {
		shopServiceReference.goodsService.updateRecommendStatus(ids, recommendStatus);
	}

	@RequiresPermissions(ShopPermissions.Goods.EDIT)
	@PostMapping("/edit/deleteStatus")
	@AjaxWrapper
	public void updateDeleteStatus(@RequestParam("ids") List<Long> ids, @RequestParam("deleteStatus") Integer deleteStatus) {
		shopServiceReference.goodsService.updateDeleteStatus(ids, deleteStatus);
	}

	@GetMapping("/detail/{goodsId}")
	public String detail(@PathVariable("goodsId") Long goodsId, Model model) {
		ShopGoods goods = shopServiceReference.goodsService.find(goodsId);
		if (goods == null) {
			throw BusinessException.build(String.format("商品不存在%s", goodsId));
		}
		model.addAttribute("goods", goods);
		return prefix + "/detail";
	}

	@RequiresPermissions(ShopPermissions.Goods.REMOVE)
	@PostMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		boolean result = shopServiceReference.goodsService.remove(ids);
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
