package me.izhong.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.sofa.rpc.common.utils.JSONUtils;
import com.aliyuncs.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.config.AliPayProperties;
import me.izhong.shop.dao.PayRecordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Map;

@Service
@Slf4j
public class AliPayService {
    @Autowired
    AlipayClient alipayClient;
    @Autowired
    AliPayProperties alipayProperties;
    @Autowired
    PayRecordDao payRecordDao;

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
     * 转账、提现
     * @param outTradeNo 支付时传入的商户订单号
     * @return
     */
    public AlipayFundTransUniTransferResponse transfer(String outTradeNo,
                                                       BigDecimal amount, String alipayAccount,
                                                       String alipayName) {
        JSONObject bizContent = new JSONObject();
        AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
        bizContent.put("out_biz_no", outTradeNo);
        bizContent.put("trans_amount", amount);
        bizContent.put("product_code", "TRANS_ACCOUNT_NO_PWD");
        bizContent.put("biz_scene", "DIRECT_TRANSFER");
        bizContent.put("order_title", "提现");

        JSONObject payee = new JSONObject();
        payee.put("identity", alipayAccount);
        payee.put("identity_type", "ALIPAY_LOGON_ID");
        payee.put("name",alipayName);

        bizContent.put("payee_info", payee);

        request.setBizContent(bizContent.toString());
        AlipayFundTransUniTransferResponse response = null;
        try {
            response = alipayClient.execute(request);
            if(response.isSuccess()){
                log.info("提现调用成功," + outTradeNo);
            } else {
                log.warn("调用失败," + outTradeNo);
            }
            log.info("提现结果应答{}", URLDecoder.decode(response.getBody(),"utf8"));
        } catch (Exception e) {
            log.error("提现异常",e);
            throw BusinessException.build("提现操作失败");
        }
        return response;
    }

    /**
     *
     * @param outTradeNo 支付时传入的商户订单号
     * @return
     */
    public AlipayFundTransOrderQueryResponse queryTransfer(String outTradeNo) {
        AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_biz_no", outTradeNo);
        request.setBizContent(bizContent.toString());
        AlipayFundTransOrderQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if(response.isSuccess()){
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }
            log.info("提现查询应答{}", URLDecoder.decode(response.getBody(),"utf8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
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

            if(response.isSuccess()){
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }
            log.info("支付结果应答{}", URLDecoder.decode(response.getBody(),"utf8"));
            return response.getBody();
        } catch (Exception e) {
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
