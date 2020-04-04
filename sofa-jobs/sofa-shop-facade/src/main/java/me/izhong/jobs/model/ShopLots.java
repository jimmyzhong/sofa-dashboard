package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

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
	private String lotsNo; // 拍品编号
	private String name;
	private String description;
	private Long goodsId;
	private Integer lotCategoryId;
	private String content;
	private BigDecimal startPrice;
	private BigDecimal addPrice;
	private BigDecimal deposit;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
	private BigDecimal reservePrice;
	private Integer platformRatio;
	private BigDecimal platformAmount;
	private BigDecimal revenueAmount;
	private BigDecimal finalPrice;
    private Integer maxMemberCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    //上传状态
    private Integer uploaded;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime uploadedTime;
    private String uploadedMsg;
}
