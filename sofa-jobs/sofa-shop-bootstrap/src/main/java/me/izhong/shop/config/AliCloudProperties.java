package me.izhong.shop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "ali-cloud")
@Component
public class AliCloudProperties {
    private String certifyServiceHost;
    private String certifyServicePath;
    private String appCode;

    private String smsDomain;
    private String smsVersion;
    private String smsRegionId;
    private String smsAccessKey;
    private String smsSecret;
    private String smsSignName;
    private String smsTemplate;
    private String smsPassResetTemplate;
}
