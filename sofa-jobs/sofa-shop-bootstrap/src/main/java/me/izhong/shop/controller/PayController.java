package me.izhong.shop.controller;

import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.sofa.rpc.common.utils.JSONUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.consts.ErrorCode;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dto.PayInfoDTO;
import me.izhong.shop.entity.Order;
import me.izhong.shop.service.IOrderService;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.service.impl.AliPayService;
import me.izhong.shop.service.impl.PayRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.izhong.shop.consts.MoneyTypeEnum.getDescriptionByState;
import static me.izhong.shop.consts.PayMethodEnum.ALIPAY;
import static me.izhong.shop.consts.PayStatusEnum.*;

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
    @Autowired
    IUserService userService;
    @Autowired
    PayRecordService payRecordService;

    @PostMapping(path="/alipay", consumes = "application/json")
    @ResponseBody
    //@RequireUserLogin
    @ApiOperation(value="发起支付请求", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PayInfoDTO pay(
            @ApiParam(required = true, type = "object", value = "支付请求, like: \n{" +
                    "  \"orderNo\": \"00001\"" +
                    "}")
            @RequestBody PayInfoDTO params) {
        if(StringUtils.isEmpty(params.getOrderNo())) {
            throw BusinessException.build("请求参数中商户订单(orderNo)不存在.");
        }

        String orderNo = params.getOrderNo();
        Order order  = orderService.findByOrderNo(orderNo);
//        Order order = getOrderForTest(params.getOrderNo());
        expectMandatoryFieldForAlipay(order);

        String payMaterials = aliPayService.pay(order.getSubject(), order.getDescription(), orderNo,
                order.getTotalAmount());
        PayInfoDTO res = new PayInfoDTO();
        res.setPayInfo(payMaterials);
        return res;
    }

    @PostMapping(path="/alipay/withdraw", consumes = "application/json")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="支付宝提现请求", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public void withDrawMoney(
            @ApiParam(required = true, type = "object", value = "支付请求, like: \n{" +
                    "  \"chargeAmount\": 100" +
                    "}")
            @RequestBody PayInfoDTO params, HttpServletRequest request) {
        if (params.getChargeAmount() == null || params.getChargeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.build("提现金额要大于0");
        }
        SessionInfo session = CacheUtil.getSessionInfo(request);
        userService.checkUserCertified(session.getId());
        String existingAlipayAccount = userService.findById(session.getId()).getAlipayAccount();
        if (StringUtils.isEmpty(existingAlipayAccount)){
            throw BusinessException.build(ErrorCode.USER_ALIPAY_ACCOUNT_NOT_EXISTS, "请绑定支付宝账号");
        }
        payRecordService.addWithdrawMoneyRecord(session.getId(), params.getChargeAmount(), existingAlipayAccount);
    }


    @PostMapping(path="/alipay/charge", consumes = "application/json")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="发起支付宝充值请求", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PayInfoDTO payForCharge(
            @ApiParam(required = true, type = "object", value = "支付请求, like: \n{" +
                    "  \"orderNo\": \"00001\"" +
                    "}")
            @RequestBody PayInfoDTO params, HttpServletRequest request) {
        if (params.getChargeAmount() == null || params.getChargeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.build("充值金额要大于0");
        }

        SessionInfo session = CacheUtil.getSessionInfo(request);
        userService.checkUserCertified(session.getId());

        if(StringUtils.isEmpty(params.getOrderNo())) {
            //generate a order number for charge request
            params.setOrderNo(orderService.generateOrderNo());
        }

        String orderNo = params.getOrderNo();
        Order order  = orderService.findByOrderNo(orderNo);
        if (order == null) {
            order = new Order();
            order.setOrderSn(orderNo);
        }
        order.setOrderType(MoneyTypeEnum.DEPOSIT_MONEY.getType());
        order.setTotalAmount(params.getChargeAmount());
        order.setUserId(session.getId());
        order.setCount(1);
        order.setSubject("余额充值");
        order.setDescription("充值金额:" + order.getTotalAmount());
        order.setStatus(OrderStateEnum.WAIT_PAYING.getState());
        order.setCreateTime(LocalDateTime.now());
        // TODO store in redis or db
        orderService.saveOrUpdate(order);
        expectMandatoryFieldForAlipay(order);

        String payMaterials = aliPayService.pay(order.getSubject(), order.getDescription(), orderNo,
                order.getTotalAmount());
        PayInfoDTO res = new PayInfoDTO();
        res.setPayInfo(payMaterials);
        res.setOrderNo(order.getOrderSn());
        return res;
    }

    @PostMapping(path="/alipay/query", consumes = "application/json")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="查询支付状态", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PayInfoDTO query(@ApiParam(required = true, type = "object", value = "支付请求, like: \n{" +
            "  \"orderNo\": \"00001\"" +
            "}")@RequestBody PayInfoDTO params){
        if(StringUtils.isEmpty(params.getOrderNo())) {
            throw BusinessException.build("请求参数中商户订单(orderNo)不存在.");
        }

        String orderNo = params.getOrderNo();
        Order order  = orderService.findByOrderNo(orderNo);
//        Order order = getOrderForTest(orderNo);

        PayInfoDTO res = new PayInfoDTO();
        res.setOrderNo(orderNo);

        AlipayTradeQueryResponse response = aliPayService.queryOrder(orderNo, params.getExternalTradeNo());

        if (!response.isSuccess()) {
            log.warn("order does not succeed." + orderNo + "," + response.getSubMsg());
            throw BusinessException.build("交易失败:" + response.getMsg());
        }

        res.setExternalTradeNo(response.getTradeNo());
        if (!StringUtils.equals(order.getOrderSn(), response.getOutTradeNo())){
            log.warn("order number mismatch." + order.getOrderSn() + ", VS " + response.getOutTradeNo());
            throw BusinessException.build("内部订单号不一致:" + order.getOrderSn() + ", VS " + response.getOutTradeNo());
        }

        BigDecimal totalAmountInResponse = BigDecimal.valueOf(Double.valueOf(response.getTotalAmount()));
        if (!order.getTotalAmount().equals(totalAmountInResponse)) {
            log.warn("order total amount mismatch." + order.getTotalAmount() + ", VS " + response.getTotalAmount());
            throw BusinessException.build("订单金额不一致:" + order.getTotalAmount() + ", VS " + response.getTotalAmount());
        }

        BigDecimal payAmountInResponse = BigDecimal.valueOf(Double.valueOf(response.getBuyerPayAmount()));
        String status = getPayStatus(response.getTradeStatus());
        String comment = getMessage(response.getMsg());
        order.setPayStatus(status);
        order.setPayTradeNo(response.getTradeNo());
        orderService.updatePayInfo(order,response.getTradeNo(), ALIPAY.name(), getDescriptionByState(order.getOrderType()), payAmountInResponse,
                order.getTotalAmount(), status, comment);
        res.setTradeStatus(status);
        return res;
    }

//    private Order getOrderForTest(String orderNo) {
//        Order order = new Order();
//        order.setOrderSn(orderNo);
//        order.setSubject("Test商品");
//        order.setDescription("11");
//        order.setTotalAmount(BigDecimal.valueOf(0.01));
//        return order;
//    }

    private String getMessage(String msgInResponse) {
        String comment = null;
        if (!StringUtils.isEmpty(msgInResponse)) {
            comment = msgInResponse;
        }
        return comment;
    }

    private String getPayStatus(String statusInResponse) {
        String status = "";
        if ("WAIT_BUYER_PAY".equalsIgnoreCase(statusInResponse)) {
            status = NOT_PAID.name();
        } else if ("TRADE_SUCCESS".equalsIgnoreCase(statusInResponse)) {
            status = SUCCESS.name();
        } else if ("TRADE_CLOSED".equalsIgnoreCase(statusInResponse)
                || "TRADE_FINISHED".equalsIgnoreCase(statusInResponse)) {
            status = CLOSE.name();
        }
        return status;
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
        log.info("notify content:" + JSONUtils.toJSONString(params));
        boolean verified = aliPayService.verify(params);
        if (!verified) {
            log.error("verify failed, " + JSONUtils.toJSONString(params));
            throw BusinessException.build("支付宝支付结果校验失败");
        }
        String orderNo = params.get("out_trade_no");
        Order order = orderService.findByOrderNo(orderNo);
//        Order order = getOrderForTest(orderNo);
        if (order == null) {
            log.error("notify out_trade_no does not exist." + orderNo);
            throw BusinessException.build("商品订单不存在." + orderNo);
        }

        String totalAmount = params.get("total_amount");
        if (!order.getTotalAmount().equals(BigDecimal.valueOf(Double.valueOf(totalAmount)))) {
            log.error("total amount mismatch. local:" + order.getTotalAmount().toString() + ", alipay:" + totalAmount);
            throw BusinessException.build("订单金额不相等");
        }


        BigDecimal payAmountInResponse = BigDecimal.valueOf(Double.valueOf(params.get("buyer_pay_amount")));
        String status = getPayStatus(params.get("trade_status"));
        order.setPayStatus(status);
        order.setPayTradeNo(params.get("trade_no"));
         orderService.updatePayInfo(order,params.get("trade_no"), ALIPAY.name(), getDescriptionByState(order.getOrderType()), payAmountInResponse,
                order.getTotalAmount(), status, null);
        log.info("trade status " + status + ", tradeNo:" + order.getPayTradeNo());
    }

    private void expectMandatoryFieldForAlipay(Order order) {
        if (StringUtils.isEmpty(order.getSubject())) {
            throw BusinessException.build("订单标题不能为空");
        }
        if (StringUtils.isEmpty(order.getDescription())) {
            throw BusinessException.build("订单表述不能为空");
        }
        if (order.getTotalAmount() == null) {
            throw BusinessException.build("订单金额不能为空");
        }
    }
}
