package me.izhong.shop.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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

    @Bean
    public AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(url, appId, appPrivateKey, format, charset,
                aliPubKey, signType);
    }
}
