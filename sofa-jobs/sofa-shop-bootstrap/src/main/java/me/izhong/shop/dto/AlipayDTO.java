package me.izhong.shop.dto;

import lombok.Data;

@Data
public class AlipayDTO {
    private String orderNo;
    private String alipayTradeNo;
    private String tradeStatus;
    private String payInfo;
}
