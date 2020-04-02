package me.izhong.shop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayInfoDTO {
    private String orderNo;
    private Long auctionId;
    private String lotsNo;
    private String externalTradeNo;
    private String tradeStatus;
    private String payInfo;
    private BigDecimal chargeAmount;
    private String account;
    private String password;
}
