package me.izhong.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.exception.BusinessException;
import me.izhong.shop.config.AliCloudProperties;
import me.izhong.shop.response.ali.CertifyServiceResponse;
import me.izhong.shop.response.ali.SmsResponse;
import me.izhong.shop.util.AliCloudUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class ThirdPartyService {
    private AliCloudProperties properties;
    private StringRedisTemplate redisTemplate;

    @Autowired
    public ThirdPartyService(AliCloudProperties properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    public boolean getCertifiedInfo(String personName, String idCard) {
        CertifyServiceResponse response = AliCloudUtils.instance.fetchCertifiedUserInfo(properties, personName, idCard);
        if (response != null) {
            if(response.isSuccess()) {
                return true;
            } else {
                throw BusinessException.build(response.getMessage());
            }
        }

        return false;
    }

    public String sendSms(String phoneNumber, JSONObject params, Boolean resetPasswordSms) {
        String paramString = params.toString();
        SmsResponse response = AliCloudUtils.instance.sendSms(properties, phoneNumber, paramString, resetPasswordSms);
        if (!response.isSuccess()) {
            return response.getCode() + "," + response.getMessage();
        }
        return null;
    }
}
