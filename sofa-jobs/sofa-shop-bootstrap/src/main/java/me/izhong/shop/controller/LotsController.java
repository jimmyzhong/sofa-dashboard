package me.izhong.shop.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.entity.LotsItem;
import me.izhong.shop.service.ILotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@AjaxWrapper
@RequestMapping(value = "/api/auction")
@Api(value = "拍卖接口",description = "拍卖相关接口描述")
public class LotsController {
    @Autowired
    ILotsService lotsService;

    @PostMapping(value = "/listOfUser")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="用户拍卖列表", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PageModel<Lots> list(@RequestBody PageQueryParamDTO query, HttpServletRequest request) {
        return lotsService.listOfUser(CacheUtil.getSessionInfo(request).getId(), query);
    }

    @PostMapping(value = "/items/{auctionId}")
    @ResponseBody
    @ApiOperation(value="拍卖出价明细", httpMethod = "POST")
    public PageModel<LotsItem> listItems(@PathVariable("auctionId")Long auctionId,  @RequestBody PageQueryParamDTO query) {
        return lotsService.listBidItems(auctionId, query);
    }

    @PostMapping(value = "/cats")
    @ResponseBody
    @ApiOperation(value="拍卖区列表", httpMethod = "POST")
    public PageModel<LotsCategory> listCats(@RequestBody PageQueryParamDTO query) {
        return lotsService.listCategory(query);
    }

    @GetMapping(value = "/cats/public")
    @ResponseBody
    @ApiOperation(value="公共拍卖区列", httpMethod = "GET")
    public Map publicCat() {
        return  new HashMap(){{put("id", "1000");}};
    }

    @PostMapping(value = "/listByCategory")
    @ResponseBody
    @ApiOperation(value="拍卖区列表", httpMethod = "POST")
    public PageModel<Lots> listLotsOfCategory(@RequestBody PageQueryParamDTO query) {
        return lotsService.listLotsOfCategory(query);
    }
}
