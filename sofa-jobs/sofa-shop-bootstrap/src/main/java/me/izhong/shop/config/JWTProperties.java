package me.izhong.shop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "jwt")
@Component
public class JWTProperties {
    private String applicationID;
    private String secret;
    private String name;
    private int expiresSecond;
}
