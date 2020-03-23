package me.izhong.shop.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@ConfigurationProperties(prefix = "alipay")
@Component
@Configuration
@Slf4j
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

    //certified, used to transfer
    String certifiedAppId;
    String certifiedAppPrivateKey;
    String certifiedAppPubKeyPath;
    String certifiedAliRootKeyPath;
    String certifiedAliPubKeyPath;

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
            certAlipayRequest.setAppId(getCertifiedAppId());
            certAlipayRequest.setPrivateKey(getCertifiedAppPrivateKey());
            certAlipayRequest.setFormat("json");
            certAlipayRequest.setCharset(getCharset());
            certAlipayRequest.setSignType(getSignType());
            certAlipayRequest.setCertPath(getCertifiedAppPubKeyPath());
            certAlipayRequest.setAlipayPublicCertPath(getCertifiedAliPubKeyPath());
            certAlipayRequest.setRootCertPath(getCertifiedAliRootKeyPath());
            return new DefaultAlipayClient(certAlipayRequest);
        }catch (AlipayApiException exp) {
            log.error("开发请修改application.yaml");
        }
        return null;
    }
}
