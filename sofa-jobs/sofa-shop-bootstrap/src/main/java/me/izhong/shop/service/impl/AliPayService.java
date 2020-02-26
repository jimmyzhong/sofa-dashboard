package me.izhong.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.aliyuncs.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.config.AliPayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class AliPayService {
    @Autowired
    AlipayClient alipayClient;
    @Autowired
    AliPayProperties alipayProperties;

    /**
     *
     * @param outTradeNo 支付时传入的商户订单号
     * @param tradeNo 支付时返回的支付宝交易号
     * @return
     */
    public String queryOrder(String outTradeNo, String tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        if (!StringUtils.isEmpty(outTradeNo)) {
            bizContent.put("out_trade_no", outTradeNo);
        }
        if (!StringUtils.isEmpty(tradeNo)) {
            bizContent.put("trade_no", tradeNo);
        }
        request.setBizContent(bizContent.toJSONString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
            return response.getBody();
        }catch (AlipayApiException e) {
            log.error("query order status outTradeNo="+outTradeNo+",tradeNo="+tradeNo, e);
            throw BusinessException.build("查询订单信息失败:" + e.getMessage());
        }
    }

    public String pay (String subject, String outtradeno, BigDecimal amount) {
        //实例化客户端
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody("我是测试数据");
        model.setSubject(subject);
        model.setOutTradeNo(outtradeno);
        model.setTimeoutExpress("30m"); //TODO read from config
        model.setTotalAmount(amount.toString());
        model.setProductCode("QUICK_MSECURITY_PAY"); //TODO read from config
        request.setBizModel(model);
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            return response.getBody();
        } catch (AlipayApiException e) {
            log.error("make order error", e);
            throw BusinessException.build("下单失败:" + e.getMessage());
        }
    }

    /**
     * verify result
     * @param request
     * @return
     */
    public boolean verify(HttpServletRequest request) {
        //获取支付宝POST过来反馈信息
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
        try {
            boolean flag = AlipaySignature.rsaCheckV1(params, alipayProperties.getAliPubKey(),
                    alipayProperties.getCharset(), alipayProperties.getSignType());
            return flag;
        }catch (AlipayApiException e){

        }
    }
}
