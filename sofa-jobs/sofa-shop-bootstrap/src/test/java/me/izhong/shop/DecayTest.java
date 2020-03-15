package me.izhong.shop;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DecayTest {

    @Test
    public void testFixedRateDecay() {
        LocalDateTime created = LocalDateTime.parse("2020-03-12 01:20:22", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Long hours = Duration.between(created, LocalDateTime.now()).toHours();
        System.out.println("hours:" + hours);
        Long e = hours / 24;

        System.out.println("e:" + e.intValue());

        Long leftHours = 24 - (hours % 24 );
        System.out.println("timeLeft:" + leftHours);

        BigDecimal newPrice = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(1-0.1).pow(e.intValue()));
        System.out.println("newPrice " + newPrice);
    }
}
