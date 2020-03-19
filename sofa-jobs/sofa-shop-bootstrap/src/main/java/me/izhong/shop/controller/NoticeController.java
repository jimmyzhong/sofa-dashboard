package me.izhong.shop.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.dto.order.OrderDTO;
import me.izhong.shop.entity.Notice;
import me.izhong.shop.service.INoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@AjaxWrapper
@RequestMapping(value = "/api/notice")
@Api(value = "公告接口",description = "公告相关接口描述")
public class NoticeController {
    @Autowired private INoticeService noticeService;

    @PostMapping(value = "/list")
    @ResponseBody
    @ApiOperation(value="通知列表", httpMethod = "POST")
    public PageModel<Notice> list(@RequestBody PageQueryParamDTO query) {
        Integer status;
        try {
            status = StringUtils.isEmpty(query.getStatus()) ? 1 : Integer.valueOf(query.getStatus());
        } catch (NumberFormatException e) {
            throw BusinessException.build("status应该是整数");
        }
        if (StringUtils.isEmpty(query.getOrderByColumn())) {
            query.setOrderByColumn("updateTime");
            query.setOrderDirection("description");
        }
        
        PageModel<Notice> model = noticeService.pageList(query, query.getQuery(), status);
        model.getRows().stream().forEach(n->n.setContent(null));
        return model;
    }

    @GetMapping(value = "/detail/{id}")
    @ResponseBody
    @ApiOperation(value="通知详情", httpMethod = "GET")
    public Notice detail(@PathVariable("id") Long id) {
        return noticeService.findById(id);
    }
}
