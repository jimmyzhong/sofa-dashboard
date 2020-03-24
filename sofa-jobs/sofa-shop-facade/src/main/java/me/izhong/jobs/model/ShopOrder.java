package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopOrder implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String orderSn;
    private String nickName;
    /** 订单总金额  */
    private BigDecimal totalAmount;
    /** 应付金额(实际支付金额) */
    private BigDecimal payAmount;
    /** 运费金额  */
    private BigDecimal freightAmount;
    /** 促销优化金额  */
    private BigDecimal promotionAmount;
    /** 积分抵扣金额  */
    private BigDecimal integrationAmount;
    /** 支付方式: 1->支付宝;2->微信 */
    private Integer payType;
    /** 订单来源: 1->h5订单;2->app订单 */
    private Integer sourceType;
    /** 订单状态: 0->待付款;1->待发货;2->已发货;3->已完成;4->已关闭;5->无效订单 */
    private Integer status;
    /** 订单类型: 1->正常订单;2->秒杀订单  */
    private Integer orderType;
    /** 物流公司(配送方式) */
    private String deliveryCompany;
    /** 物流单号 */
    private String deliverySn;
    /** 自动确认时间(天) */
    private Integer autoConfirmDay;
    /** 可以获得的积分 */
    private Integer integration;

    /** 活动信息 */
    private String promotionInfo;
    /** 收货人姓名 */
    private String receiverName;
    /** 收货人电话 */
    private String receiverPhone;
    /** 收货人邮编 */
    private String receiverPostCode;
    /** 省份/直辖市  */
    private String receiverProvince;
    /** 城市 */
    private String receiverCity;
    /** 区  */
    private String receiverRegion;
    /** 详细地址 */
    private String receiverDetailAddress;
    /** 订单备注 */
    private String note;
    /** 确认收货状态: 0->未确认;1->已确认 */
    private Integer confirmStatus;
    /** 删除状态: 0->未删除;1->已删除 */
    private Integer isDelete;
    /** 下单时使用的积分 */
    private Integer useIntegration;
    /** 支付时间 */
    private LocalDateTime paymentTime;
    /** 发货时间 */
    private LocalDateTime deliveryTime;
    /** 确认收货时间 */
    private LocalDateTime receiveTime;
    /** 评价时间 */
    private LocalDateTime commentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
