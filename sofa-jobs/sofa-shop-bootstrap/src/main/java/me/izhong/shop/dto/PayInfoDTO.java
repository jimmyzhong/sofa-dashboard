package me.izhong.shop.dto;

import lombok.Data;

@Data
public class PayInfoDTO {
    private String orderNo;
    private String externalTradeNo;
    private String tradeStatus;
    private String payInfo;
}
