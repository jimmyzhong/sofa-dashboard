package me.izhong.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "TX_ORDER")
@SequenceGenerator(name = "ORDER_SEQ", sequenceName = "ORDER_SEQ", allocationSize = 1)
public class Order {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "USER_ID")
    private Long userId;
    @Column(name = "ORDER_SN")
    private String orderSn;
    @Column(name = "NICK_NAME")
    private String nickName;
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;
    @Column(name = "PAY_AMOUNT")
    private BigDecimal payAmount;
    @Column(name = "FREIGHT_AMOUNT")
    private BigDecimal freightAmount;
    @Column(name = "PROMOTION_AMOUNT")
    private BigDecimal promotionAmount;
    @Column(name = "INTEGRATION_AMOUNT")
    private BigDecimal integrationAmount;
    @Column(name = "PAY_TYPE")
    private Integer payType;
    @Column(name = "SOURCE_TYPE")
    private Integer sourceType;
    @Column(name = "STATUS")
    private Integer status;
    @Column(name = "ORDER_TYPE")
    private Integer orderType;
    @Column(name = "DELIVERY_COMPANY")
    private String deliveryCompany;
    @Column(name = "DELIVERY_SN")
    private String deliverySn;
    @Column(name = "AUTO_CONFIRM_DAY")
    private Integer autoConfirmDay;
    @Column(name = "INTEGRATION")
    private Integer integration;
    @Column(name = "PROMOTION_INFO")
    private String promotionInfo;
    @Column(name = "RECEIVER_NAME")
    private String receiverName;
    @Column(name = "RECEIVER_PHONE")
    private String receiverPhone;
    @Column(name = "RECEIVER_POST_CODE")
    private String receiverPostCode;
    @Column(name = "RECEIVER_PROVINCE")
    private String receiverProvince;
    @Column(name = "RECEIVER_CITY")
    private String receiverCity;
    @Column(name = "RECEIVER_REGION")
    private String receiverRegion;
    @Column(name = "RECEIVER_DETAIL_ADDRESS")
    private String receiverDetailAddress;
    @Column(name = "NOTE")
    private String note;
    @Column(name = "CONFIRM_STATUS")
    private Integer confirmStatus;
    @Column(name = "IS_DELETE")
    private Integer isDelete;
    @Column(name = "USE_INTEGRATION")
    private Integer useIntegration;
    @Column(name = "PAYMENT_TIME")
    private LocalDateTime paymentTime;
    @Column(name = "DELIVERY_TIME")
    private LocalDateTime deliveryTime;
    @Column(name = "RECEIVE_TIME")
    private LocalDateTime receiveTime;
    @Column(name = "COMMENT_TIME")
    private LocalDateTime commentTime;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
    @Column(name = "SUBJECT", length = 200)
    private String subject; // 订单标题
    @Column(name = "DESCRIPTION", length = 500)
    private String description; // 订单的描述
    @Column(name = "PAY_TRADE_NO", length = 50)
    private String payTradeNo;
    @Column(name = "PAY_STATUS", length = 10)
    private String payStatus;
    @Column(name = "COUNT")
    private Integer count;
    @Column(name = "RESALE_USER")
    private Long resaleUser;
    // first product info
    @Column(name = "PRODUCT_PIC")
    private String productPic;
    @Column(name = "UNIT_PRICE")
    private BigDecimal unitPrice;
}
