package me.izhong.shop.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@ConfigurationProperties(prefix = "alipay")
@Component
@Configuration
public class AliPayProperties {
    String url;
    String appId;
    String appPrivateKey;
    String format;
    String charset;
    String aliPubKey;
    String signType;
    String notifyUrl;
    String productCode;
    String orderExpire;
    String appPubKeyPath;
    String aliPubKeyPath;
    String aliRootKeyPath;

//    @PostConstruct
//    public void setUp(){
//        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//    }

    @Bean
    public AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(url, appId, appPrivateKey, format, charset,
                aliPubKey, signType);
    }

    @Bean
    public AlipayClient getAlipayCertifiedClient() {
        try {
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(getUrl());
            certAlipayRequest.setAppId(getAppId());
            certAlipayRequest.setPrivateKey(getAppPrivateKey());
            certAlipayRequest.setFormat("json");
            certAlipayRequest.setCharset(getCharset());
            certAlipayRequest.setSignType(getSignType());
            certAlipayRequest.setCertPath(getAppPubKeyPath());
            certAlipayRequest.setAlipayPublicCertPath(getAliPubKeyPath());
            certAlipayRequest.setRootCertPath(getAliRootKeyPath());
            return new DefaultAlipayClient(certAlipayRequest);
        }catch (AlipayApiException exp) {
        }
        return null;
    }
}
