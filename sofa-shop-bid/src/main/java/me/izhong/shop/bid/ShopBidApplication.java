package me.izhong.shop.bid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;

@SpringBootApplication(exclude = {JacksonAutoConfiguration.class},
        scanBasePackages = {"me.izhong"})
public class ShopBidApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopBidApplication.class, args);
    }

}
