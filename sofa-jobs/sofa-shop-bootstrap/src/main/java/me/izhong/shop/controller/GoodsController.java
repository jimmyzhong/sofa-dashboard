package me.izhong.shop.controller;

import io.swagger.annotations.Api;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AjaxWrapper
@RequestMapping(path="/api/goods")
@Api(value = "商品相关接口",description = "商品相关接口描述")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @PostMapping(path = "/list")
    public PageModel<GoodsDTO> list(PageQueryParamDTO pageQuery) {
        pageQuery.validRequest();
        return goodsService.list(pageQuery);
    }
}
