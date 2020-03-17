package me.izhong.shop.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.service.IGoodsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@AjaxWrapper
@RequestMapping(path="/api/goods")
@Api(value = "商品相关接口",description = "商品相关接口描述")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @PostMapping(path = "/list/index", consumes = "application/json")
    @ResponseBody
    @ApiOperation(value="首页商品", httpMethod = "POST", consumes = "application/json")
    public PageModel<GoodsDTO> listOnIndexPage(@RequestBody PageQueryParamDTO pageQuery) {
        pageQuery.validRequest();

        pageQuery.setOnIndexPage(true);
        return goodsService.list(pageQuery);
    }

    @PostMapping(path = "/list", consumes = "application/json")
    @ResponseBody
    @ApiOperation(value="获取商品列表", httpMethod = "POST", consumes = "application/json")
    public PageModel<GoodsDTO> list(@RequestBody PageQueryParamDTO pageQuery) {
        pageQuery.validRequest();
        return goodsService.list(pageQuery);
    }

    @GetMapping(path = "/detail/{id}")
    @ResponseBody
    @ApiOperation(value="获取商品详情", httpMethod = "GET")
    public GoodsDTO detail(@PathVariable("id") Long id) {
        return goodsService.findGoodsWithAttrById(id);
    }
}
