package me.izhong.shop.service;

import me.izhong.shop.config.AliCloudProperties;
import me.izhong.shop.response.ali.CertifyServiceResponse;
import me.izhong.shop.response.ali.SmsResponse;
import me.izhong.shop.util.AliCloudUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThirdPartyService {
    private AliCloudProperties properties;

    @Autowired
    public ThirdPartyService(AliCloudProperties properties){
        this.properties = properties;
    }

    public String getCertifiedInfo(String personName, String idCard) {
        CertifyServiceResponse response = AliCloudUtils.instance.fetchCertifiedUserInfo(properties, personName, idCard);
        if (response != null && response.isSuccess()) {
            return response.getResult();
        }
        return null;
    }

    public String sendSms(String phoneNumber) {
        SmsResponse response = AliCloudUtils.instance.sendSms(properties, phoneNumber);
        if (!response.isSuccess()) {
            return response.getCode() + "," + response.getMessage();
        }
        return null;
    }
}
