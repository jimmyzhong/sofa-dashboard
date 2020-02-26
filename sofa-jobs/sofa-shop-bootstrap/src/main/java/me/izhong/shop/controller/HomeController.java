package me.izhong.shop.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.izhong.shop.dto.GoodsCategoryDTO;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IHomeService;

@Controller
@RequestMapping("/api/home")
public class HomeController {

	@Autowired
	private IHomeService homeService;

    @GetMapping(value = "/recommendProductList")
    @ResponseBody
    public Map<String, Object> recommendProductList(
    		@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
    		@RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<Goods> page = homeService.recommendProductList(pageNum, pageSize);
        Map<String, Object> map = Maps.newHashMap();
        map.put("page", page);
        return map;
    }

    @GetMapping(value = "/productCategoryList/{parentId}")
    @ResponseBody
    public Map<String, Object> productCategoryList(@PathVariable Long parentId) {
        List<GoodsCategory> goodsCategoryList = homeService.productCategoryList(parentId);
        List<GoodsCategoryDTO> dtoList = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(goodsCategoryList)) {
        	dtoList = goodsCategoryList.stream().map(t -> {
        		GoodsCategoryDTO dto = new GoodsCategoryDTO();
        		BeanUtils.copyProperties(t, dto);
                return dto;
        	}).collect(Collectors.toList());
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("goodsCategoryList", dtoList);
        return map;
    }
}
