package me.izhong.shop.dto.order;

import lombok.Data;
import me.izhong.shop.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderFullDTO {

    private Long id;
    private Long userId;
    private String orderSn;
    private String nickName;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal promotionAmount;
    private BigDecimal integrationAmount;
    private Integer payType;
    private Integer sourceType;
    private Integer status;
    private String statusComment;
    private Integer orderType;
    private String deliveryCompany;
    private String deliverySn;
    private Integer autoConfirmDay;
    private Integer integration;
    private String promotionInfo;
    private String receiverName;
    private String receiverPhone;
    private String receiverPostCode;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    private String note;
    private Integer confirmStatus;
    private Integer isDelete;
    private Integer useIntegration;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime commentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String subject; // 订单标题
    private String description; // 订单的描述
    private String payTradeNo;
    private String payStatus;
    private Integer count;

    private List<OrderItem> items;
}
