package me.izhong.shop.dto.order;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.math.BigDecimal;

@Data
@Builder
public class OrderDTO {
    @Tolerate
    public OrderDTO() {

    }
    private Long id;
    private Long userId;
    private String orderSn;
    private BigDecimal totalAmount;
    private String statusComment;
    private String subject; // 订单标题
    private String description; // 订单的描述
    private String payTradeNo;
    private String payStatus;
    private Integer count;
}