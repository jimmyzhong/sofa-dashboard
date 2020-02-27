package me.izhong.shop.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.rpc.common.utils.JSONUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.config.Constants;
import me.izhong.shop.dto.AlipayDTO;
import me.izhong.shop.entity.Order;
import me.izhong.shop.service.IOrderService;
import me.izhong.shop.service.impl.AliPayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@AjaxWrapper
@Api(value = "支付接口",description = "支付相关接口描述")
@RequestMapping(value = "/api/pay")
@Slf4j
public class PayController {

    @Autowired
    IOrderService orderService;
    @Autowired
    AliPayService aliPayService;

    @PostMapping(path="/alipay", consumes = "application/json")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="发起支付请求", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public AlipayDTO pay(
            @ApiParam(required = true, type = "object", value = "支付请求, like: \n{" +
                    "  \"orderNo\": \"00001\"" +
                    "}")
            @RequestBody AlipayDTO params) {
        if(StringUtils.isEmpty(params.getOrderNo())) {
            throw BusinessException.build("请求参数中商户订单(orderNo)不存在.");
        }

        Long orderNo = Long.valueOf(params.getOrderNo());
        Order order  = orderService.findById(orderNo);
        expectMandatoryFieldForAlipay(order);

        String alipayTradeNo = aliPayService.pay(order.getSubject(), order.getDescription(), orderNo.toString(),
                order.getTotalAmount());

        order.setPayTradeNo(alipayTradeNo);
        orderService.saveOrUpdate(order);

        AlipayDTO res = new AlipayDTO();
        res.setOrderNo(params.getOrderNo());
        res.setAlipayTradeNo(alipayTradeNo);
        return res;
    }

    @PostMapping(path="/alipay/query", consumes = "application/json")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="查询支付状态", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public AlipayDTO query(@ApiParam(required = true, type = "object", value = "支付请求, like: \n{" +
            "  \"orderNo\": \"00001\"" +
            "}")@RequestBody AlipayDTO params){
        if(StringUtils.isEmpty(params.getOrderNo())) {
            throw BusinessException.build("请求参数中商户订单(orderNo)不存在.");
        }

        Long orderNo = Long.valueOf(params.getOrderNo());
        Order order  = orderService.findById(orderNo);
        Map<String, String> respnse = aliPayService.queryOrder(order.getId().toString(), order.getPayTradeNo());

        String status = respnse.get("trade_status");
        order.setPayStatus(status);
        orderService.saveOrUpdate(order);

        AlipayDTO res = new AlipayDTO();
        res.setOrderNo(orderNo.toString());
        res.setAlipayTradeNo(order.getPayTradeNo());
        res.setTradeStatus(status);
        return res;
    }

    @PostMapping(path="/alipay/notify")
    @ApiOperation(value="支付结果通知", httpMethod = "POST")
    public void notify(HttpServletRequest request){
        Map<String,String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            if (values != null && values.length > 0) {
                valueStr = Stream.of(values).collect(Collectors.joining(","));
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean verified = aliPayService.verify(params);
        if (!verified) {
            log.error("verify failed, " + JSONUtils.toJSONString(params));
            throw BusinessException.build("支付宝支付结果校验失败");
        }
        String orderNo = params.get("out_trade_no");
        Order order = orderService.findById(Long.valueOf(orderNo));

        // TODO amount verify
        if (params.containsKey("total_amount")) {
            String totalAmount = params.get("total_amount");
            if (!order.getTotalAmount().equals(BigDecimal.valueOf(Double.valueOf(totalAmount)))) {
                log.error("total amount mismatch. local:" + order.getTotalAmount().toString() + ", alipay:" + totalAmount);
                throw BusinessException.build("订单金额不相等");
            }
        }
        if (params.containsKey("buyer_pay_amount")) {

        }


        String tradeStatus = params.get("trade_status");
        if (!StringUtils.equals(tradeStatus, order.getPayStatus())) {
            order.setPayStatus(tradeStatus);
            orderService.saveOrUpdate(order);
        }
    }

    private void expectMandatoryFieldForAlipay(Order order) {
        if (StringUtils.isEmpty(order.getSubject())) {
            throw BusinessException.build("订单标题不能为空");
        }
        if (StringUtils.isEmpty(order.getDescription())) {
            throw BusinessException.build("订单表述不能为空");
        }
        if (order.getPayAmount() == null) {
            throw BusinessException.build("订单金额不能为空");
        }
    }
}
