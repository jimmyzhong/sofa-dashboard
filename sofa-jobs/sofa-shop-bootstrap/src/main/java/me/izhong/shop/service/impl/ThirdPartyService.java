package me.izhong.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.config.AliCloudProperties;
import me.izhong.shop.response.ali.CertifyServiceResponse;
import me.izhong.shop.response.ali.SmsResponse;
import me.izhong.shop.util.AliCloudUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
            if(response.isSuccess() && response.getData() != null &&
                "1".equals(response.getData().get("result"))) {
                return true;
            } else if(!response.isSuccess()){
                throw BusinessException.build(response.getMessage());
            }
        }

        return false;
    }

    public String sendSms(String phoneNumber, JSONObject params, Boolean resetPasswordSms) {
        String paramString = params.toString();
        SmsResponse response = AliCloudUtils.instance.sendSms(properties, phoneNumber, paramString, resetPasswordSms);
        if (!response.isSuccess()) {
            log.info("{} 发送短信异常，阿里云响应 {}",phoneNumber,response.getMessage());
            throw BusinessException.build("短信发送失败，稍后再试。");
            //return response.getCode() + "," + response.getMessage();
        }
        return null;
    }

    public String uploadFile(String fileName, MultipartFile file) {
        try {
            AliCloudUtils.instance.uploadStream(properties, fileName, file.getInputStream());
        } catch (IOException e) {
            log.error("upload file error", e);
            throw BusinessException.build("上传文件失败");
        }
        return properties.getOssPicAccessUrl() + fileName;
    }

    public String generateUploadPicUrl(String fileName) {
        return properties.getOssPicAccessUrl() + fileName;
    }
}
