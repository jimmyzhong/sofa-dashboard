package me.izhong.shop.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.shop.dto.GoodsCategoryDTO;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.service.IGoodsCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@AjaxWrapper
@RequestMapping(path="/api/category")
@Api(value = "商品类别相关接口",description = "商品类别相关接口描述")
public class GoodsCategoryController {
    @Autowired
    private IGoodsCategoryService categoryService;

    @PostMapping(path = "/list", consumes = "application/json")
    @ApiOperation(value="获取商品类别列表", httpMethod = "POST", consumes = "application/json")
    public PageModel<GoodsCategoryDTO> list(@RequestBody PageQueryParamDTO pageQuery) {
        pageQuery.validRequest();
        if(StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            pageQuery.setOrderDirection("asc");
            pageQuery.setOrderByColumn("sort");
        }
        return categoryService.list(pageQuery);
    }
}
