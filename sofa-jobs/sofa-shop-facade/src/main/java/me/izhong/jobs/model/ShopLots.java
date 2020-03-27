package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopLots implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String name;
	private String description;
	private Long goodsId;
	private Long lotCategoryId;
	private String content;
	private BigDecimal startPrice;
	private BigDecimal addPrice;
	private BigDecimal deposit;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer userLevel;
	private String password;
	private Integer followCount;
	private BigDecimal salePrice;
	private BigDecimal nowPrice;
	private Integer payStatus;
	private String orderSn;
	private LocalDateTime paidAt;
	private String payMethod;
	private String payNo;
	private Integer isApply;
	private Integer applyType;
	private Integer applyStatus;
	private Integer isRepublish;
	private BigDecimal warningPrice;
	private Integer platformRatio;
	private BigDecimal platformAmount;
	private BigDecimal revenueAmount;
	private BigDecimal finalPrice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
