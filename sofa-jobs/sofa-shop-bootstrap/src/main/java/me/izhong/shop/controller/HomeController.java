package me.izhong.shop.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.dto.order.OrderDTO;
import me.izhong.shop.entity.Ad;
import me.izhong.shop.entity.AppVersions;
import me.izhong.shop.service.IAdService;
import me.izhong.shop.service.IAppVersionsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.izhong.shop.dto.GoodsCategoryDTO;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IHomeService;

import javax.servlet.http.HttpServletRequest;

@Controller
@AjaxWrapper
@RequestMapping("/api/home")
public class HomeController {

	@Autowired
	private IHomeService homeService;

	@Autowired
    private IAdService adService;

	@Autowired
    private IAppVersionsService appVersionsService;

    @PostMapping(value = "/ad/list")
    @ResponseBody
    @ApiOperation(value="广告列表", httpMethod = "POST")
    public PageModel<Ad> list(@RequestBody PageQueryParamDTO query, HttpServletRequest request) {
        return adService.pageList(query, null, null , null);
    }

    @GetMapping(value = "/ad/{adId}")
    @ResponseBody
    @ApiOperation(value="广告内容", httpMethod = "GET")
    public Ad detailAd(@PathVariable("adId") String adId) {
        return adService.findById(Long.valueOf(adId));
    }

    @GetMapping(value = "/version/{appType}")
    @ResponseBody
    @ApiOperation(value="版本", httpMethod = "GET")
    public AppVersions latestAppversion(@PathVariable("appType") String appType) {
        return appVersionsService.latest(appType);
    }

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
