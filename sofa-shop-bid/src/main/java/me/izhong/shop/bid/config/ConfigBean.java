package me.izhong.shop.bid.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ConfigBean {

    private int port = 9999;

    private int httpIdleTime = 120;

    private String charset = "utf-8";


    private int minTaskExecutorPoolSize = 8;
    private int maxTaskExecutorPoolSize = 1024;
    private int scheduledTaskExecutorPoolSize = 64;
    private int idleTime = 600;
}
