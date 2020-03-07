package me.izhong.jobs.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.Convert;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.dto.CategoryDTO;
import me.izhong.jobs.model.ShopGoodsCategory;

@Slf4j
@Controller
@RequestMapping("/ext/shop/category")
public class ShopGoodsCategoryController {

	private String prefix = "ext/shop/category";

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping
	public String category() {
		return prefix + "/category";
	}

	@PostMapping("/list/{parentId}")
    @AjaxWrapper
	public PageModel<ShopGoodsCategory> pageList(HttpServletRequest request, @PathVariable Long parentId) {
		PageModel<ShopGoodsCategory> page = shopServiceReference.goodsCategoryService.pageList(PageRequestUtil.fromRequest(request), parentId);
		return page;
	}

	@GetMapping("/subCategory/{categoryId}")
	public String subCategory(@PathVariable("categoryId") Long categoryId, Model model) {
		if (categoryId == null) {
			throw BusinessException.build("categoryId不能为空");
		}
		model.addAttribute("categoryId", categoryId);
		return prefix + "/subCategory";
	}

	@GetMapping("/queryLv1")
    @ResponseBody
	public Map<String, Object> queryLevel1() {
		List<CategoryDTO> dtoList = shopServiceReference.goodsCategoryService.queryLevel1();
		Map<String, Object> data = new HashMap<>();
		data.put("list", dtoList);
		return data;
	}

	@GetMapping("/queryAll")
    @ResponseBody
	public Map<String, Object> queryAll() {
		List<CategoryDTO> dtoList = shopServiceReference.goodsCategoryService.queryAll();
		Map<String, Object> data = new HashMap<>();
		data.put("categoryList", dtoList);
		return data;
	}

    @GetMapping("/add/{parentId}")
    public String add(HttpServletRequest request, @PathVariable Long parentId, Model model) {
		model.addAttribute("parentId", parentId);
        return prefix + "/add";
    }

    @PostMapping("/add")
    @AjaxWrapper
    public void addGoodsCategory(ShopGoodsCategory goodsCategory) {
    	log.info("add category =>{}", goodsCategory);
    	shopServiceReference.goodsCategoryService.create(goodsCategory);
    }

	@GetMapping("/edit/{categoryId}")
	public String edit(@PathVariable("categoryId") Long categoryId, Model model) {
		if (categoryId == null) {
			throw BusinessException.build("categoryId不能为空");
		}
		ShopGoodsCategory goodsCategory = shopServiceReference.goodsCategoryService.findById(categoryId);
		if (goodsCategory == null) {
			throw BusinessException.build(String.format("商品类目不存在%s", categoryId));
		}
		log.info("goodsCategoryDetail =>{}", goodsCategory);
		model.addAttribute("goodsCategory", goodsCategory);
		return prefix + "/edit";
	}

	@PostMapping("/detail/{categoryId}")
	@AjaxWrapper
	public ShopGoodsCategory detail(@PathVariable("categoryId") Long categoryId, Model model) {
		if (categoryId == null) {
			throw BusinessException.build("categoryId不能为空");
		}
		ShopGoodsCategory goodsCategory = shopServiceReference.goodsCategoryService.findById(categoryId);
		if (goodsCategory == null) {
			throw BusinessException.build(String.format("商品类目不存在%s", categoryId));
		}
		log.info("goodsCategoryDetail =>{}", goodsCategory);
		return goodsCategory;
	}



	@PostMapping("/edit")
	@AjaxWrapper
	public void edit(ShopGoodsCategory goodsCategory) {
		ShopGoodsCategory obj = shopServiceReference.goodsCategoryService.findById(goodsCategory.getId());
		if (obj == null) {
			throw BusinessException.build(String.format("商品类目不存在%s", goodsCategory.getId()));
		}
		shopServiceReference.goodsCategoryService.edit(goodsCategory);
	}

	@PostMapping("/edit/showStatus")
	@AjaxWrapper
	public void updatePublishStatus(@RequestParam("ids") List<Long> ids, @RequestParam("showStatus") Integer showStatus) {
		shopServiceReference.goodsCategoryService.updateShowStatus(ids, showStatus);
	}

	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		Long[] jobIds = Convert.toLongArray(ids);
		for (Long jobId : jobIds) {
			boolean rt = shopServiceReference.goodsCategoryService.remove(jobId);
			if (!rt) {
				throw BusinessException.build("删除失败");
			}
		}
	}
}
