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
	private Long lotCategoryId;
    @Column(name = "CONTENT")
	private String content;
    @Column(name = "START_PRICE")
	private BigDecimal startPrice;
    @Column(name = "ADD_PRICE")
	private BigDecimal addPrice;
    @Column(name = "DEPOSIT")
	private BigDecimal deposit;
    @Column(name = "START_TIME")
	private LocalDateTime startTime;
    @Column(name = "END_TIME")
	private LocalDateTime endTime;
    @Column(name = "USER_LEVEL")
	private Integer userLevel;
    @Column(name = "PASSWORD")
	private String password;
    @Column(name = "FOLLOW_COUNT")
	private Integer followCount;
    @Column(name = "SALE_PRICE")
	private BigDecimal salePrice;
    @Column(name = "NOW_PRICE")
	private BigDecimal nowPrice;
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
    @Column(name = "IS_REPUBLISH")
	private Integer isRepublish;
    @Column(name = "WARNING_PRICE")
	private BigDecimal warningPrice;
    @Column(name = "PLATFORM_RATIO")
	private Integer platformRatio;
    @Column(name = "PLATFORM_AMOUNT")
	private BigDecimal platformAmount;
    @Column(name = "REVENUE_AMOUNT")
	private BigDecimal revenueAmount;
    @Column(name = "FINAL_PRICE")
	private BigDecimal finalPrice;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
