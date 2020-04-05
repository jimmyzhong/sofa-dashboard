package me.izhong.shop.controller;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.dto.LotsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.entity.LotsItem;
import me.izhong.shop.entity.LotsItemStats;
import me.izhong.shop.service.ILotsService;
import me.izhong.shop.service.IUserService;
import org.springframework.beans.BeanUtils;
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
    @Autowired
    IUserService userService;

    @PostMapping(value = "/listOfUser")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="用户拍卖列表", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PageModel<LotsDTO> list(@RequestBody PageQueryParamDTO query, HttpServletRequest request) {
        return lotsService.listOfUser(CacheUtil.getSessionInfo(request).getId(), query);
    }

    @PostMapping(value = "/items/{lotsNo}")
    @ResponseBody
    @ApiOperation(value="拍卖出价明细", httpMethod = "POST")
    public PageModel<LotsItem> listItems(@PathVariable("lotsNo")String lotsNo,  @RequestBody PageQueryParamDTO query) {
        return lotsService.listBidItems(lotsNo, query);
    }

    @PostMapping(value = "/stats/{lotsNo}")
    @ResponseBody
    @ApiOperation(value="拍卖统计明细", httpMethod = "POST")
    public PageModel<LotsItemStats> listStatsItems(@PathVariable("lotsNo")String lotsNo, @RequestBody PageQueryParamDTO query) {
        return lotsService.listStatsItems(lotsNo, query);
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
    public PageModel<LotsDTO> listLotsOfCategory(@RequestBody PageQueryParamDTO query) {
        return lotsService.listLotsOfCategory(query);
    }

    @PostMapping(value = "/list/new")
    @ResponseBody
    @ApiOperation(value="新手专区", httpMethod = "POST")
    public PageModel<LotsDTO> listLotsOfNewCategory(@RequestBody PageQueryParamDTO query) {
        query.setRequiredAuctionMargin(200L);
        query.setPublicCategoryId(Integer.valueOf(publicCat().get("id").toString()));
        return lotsService.listLotsOfCategory(query);
    }
    @PostMapping(value = "/list/zero")
    @ResponseBody
    @ApiOperation(value="0元专区", httpMethod = "POST")
    public PageModel<LotsDTO> listLotsOfZeroCategory(@RequestBody PageQueryParamDTO query) {
        query.setRequiredAuctionMargin(0L);
        query.setPublicCategoryId(Integer.valueOf(publicCat().get("id").toString()));
        return lotsService.listLotsOfCategory(query);
    }
    @PostMapping(value = "/list/vip")
    @ResponseBody
    @ApiOperation(value="vip专区", httpMethod = "POST")
    public PageModel<LotsDTO> listLotsOfVipCategory(@RequestBody PageQueryParamDTO query) {
        query.setPublicCategoryId(Integer.valueOf(publicCat().get("id").toString()));
        query.setIsVip(true);
        return lotsService.listLotsOfCategory(query);
    }

    @PostMapping(value = "/list/time")
    @ResponseBody
    @ApiOperation(value="按时间筛选公共专区", httpMethod = "POST")
    public PageModel<LotsDTO> listLotsOfPubCategoryBetween(@RequestBody PageQueryParamDTO query) {
        if (query.getStartTime() == null || query.getEndTime() == null) {
            throw BusinessException.build("请指定起始结束时间");
        }
        query.setPublicCategoryId(Integer.valueOf(publicCat().get("id").toString()));
        return lotsService.listLotsOfCategory(query);
    }

    @PostMapping(value = "/list/agent")
    @ResponseBody
    @ApiOperation(value="高手专区(代理)", httpMethod = "POST")
    public PageModel<LotsDTO> listLotsOfAgentCategory(@RequestBody PageQueryParamDTO query) {
        query.setPublicCategoryId(Integer.valueOf(publicCat().get("id").toString()));
        query.setIsAgent(true);
        return lotsService.listLotsOfCategory(query);
    }

    @GetMapping(value = "/detail/{lotsNo}")
    @ResponseBody
    @ApiOperation(value="拍卖详情", httpMethod = "GET")
    public LotsDTO detail(@PathVariable("lotsNo") String lotsNo) {
        Lots lots = lotsService.findByLotsNo(lotsNo);
        if (lots == null) {
            throw BusinessException.build("拍品不存在:" + lotsNo);
        }
        LotsDTO dto = new LotsDTO();
        BeanUtils.copyProperties(lots, dto, "albumPics");
        if (lots.getAlbumPics() != null) {
            dto.setAlbumPics(JSONArray.parseArray(lots.getAlbumPics(), String.class));
        }
        return dto;
    }

    @GetMapping(value = "/check/signUp/{lotsNo}")
    @RequireUserLogin
    @ResponseBody
    @ApiOperation(value="查看当前用户是否报名", httpMethod = "GET")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public Map checkIfSignUp(@PathVariable("lotsNo") String lotsNo, HttpServletRequest request) {
        Lots lots = lotsService.findByLotsNo(lotsNo);
        if (lots == null) {
            throw BusinessException.build("拍品不存在:" + lotsNo);
        }

        boolean signUp;
        try {
            signUp = userService.checkIfUserSignUpAuction(CacheUtil.getSessionInfo(request).getId(), lots.getId());
        }catch (BusinessException e) {
            signUp = false;
        }
        Map map = new HashMap();
        map.put("signUp", signUp);
        return map;
    }
}
