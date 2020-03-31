package me.izhong.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "LOTS")
public class Lots {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "NAME")
	private String name;
    @Column(name = "DESCRIPTION")
	private String description;
    @Column(name = "GOODS_ID")
	private Long goodsId;
    @Column(name = "LOT_CATEGORY_ID")
	private Integer lotCategoryId;
    @Column(name = "CONTENT")
	private String content;
    //起拍价
    @Column(name = "START_PRICE")
	private BigDecimal startPrice;
    //加价幅度
    @Column(name = "ADD_PRICE")
	private BigDecimal addPrice;
    //保证金
    @Column(name = "DEPOSIT")
	private BigDecimal deposit;
    //拍卖开始时间
    @Column(name = "START_TIME")
	private LocalDateTime startTime;
    //拍卖结束时间
    @Column(name = "END_TIME")
	private LocalDateTime endTime;
    //用户参与拍卖的级别
    @Column(name = "USER_LEVEL")
	private Integer userLevel;
    //房间密码
    @Column(name = "PASSWORD")
	private String password;
    @Column(name = "FOLLOW_COUNT")
	private Integer followCount;
    //销售价
    @Column(name = "SALE_PRICE")
	private BigDecimal salePrice;
    //现价
    @Column(name = "NOW_PRICE")
	private BigDecimal nowPrice;
    //拍卖状态
    @Column(name = "PAY_STATUS")
	private Integer payStatus;
    @Column(name = "ORDER_SN")
	private String orderSn;
    @Column(name = "PAID_AT")
	private LocalDateTime paidAt;
    @Column(name = "PAY_METHOD")
	private String payMethod;
    @Column(name = "PAY_NO")
	private String payNo;
    @Column(name = "IS_APPLY")
	private Integer isApply;
    @Column(name = "APPLY_TYPE")
	private Integer applyType;
    @Column(name = "APPLY_STATUS")
	private Integer applyStatus;
    @Column(name = "IS_OVER")
    private Boolean over;  //是否成交
    //是否发布
    @Column(name = "IS_REPUBLISH")
	private Integer isRepublish;
    //警示价
    @Column(name = "WARNING_PRICE")
	private BigDecimal warningPrice;
    //平台扣佣比例
    @Column(name = "PLATFORM_RATIO")
	private Integer platformRatio;
    //平台佣金
    @Column(name = "PLATFORM_AMOUNT")
	private BigDecimal platformAmount;
    //平台返佣
    @Column(name = "REVENUE_AMOUNT")
	private BigDecimal revenueAmount;
    //成交价
    @Column(name = "FINAL_PRICE")
	private BigDecimal finalPrice;
    @Column(name = "FINAL_USER")
    private Long finalUser;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
    @Column(name = "FREQUENCY")
    private Integer auctionFrequency; // 出价频率
    @Column(name = "VIP_LEVEL")
    private Integer vipLevel; // vip等级
}
