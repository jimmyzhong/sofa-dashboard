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
import com.alipay.sofa.rpc.common.utils.JSONUtils;
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
    public AlipayTradeQueryResponse queryOrder(String outTradeNo, String tradeNo) {
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
            return alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
        }catch (AlipayApiException e) {
            log.error("query order status outTradeNo="+outTradeNo+",tradeNo="+tradeNo, e);
            throw BusinessException.build("查询订单信息失败:" + e.getMessage());
        }
    }

    /**
     * 支付宝下单
     * @param subject
     * @param description
     * @param outtradeno
     * @param amount
     * @return 支付宝订单号
     */
    public String pay (String subject, String description, String outtradeno, BigDecimal amount) {
        //实例化客户端
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody(description);
        model.setSubject(subject);
        model.setOutTradeNo(outtradeno);
        model.setTimeoutExpress(alipayProperties.getOrderExpire()); //TODO read from config
        model.setTotalAmount(amount.toString());
        model.setProductCode(alipayProperties.getProductCode()); //TODO read from config
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
     * @param
     * @return
     */
    public boolean verify(Map<String, String> params) {
        try {
            boolean flag = AlipaySignature.rsaCheckV1(params, alipayProperties.getAliPubKey(),
                    alipayProperties.getCharset(), alipayProperties.getSignType());
            return flag;
        }catch (AlipayApiException e){
            log.error("verify error with params: "+ JSONUtils.toJSONString(params), e);
            return false;
        }
    }
}
