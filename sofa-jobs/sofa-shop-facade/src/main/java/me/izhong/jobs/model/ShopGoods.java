package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopGoods implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long productCategoryId;
	private Long productAttributeCategoryId;
	private String name;
	private String pic;
	private String productSn;
	private Integer productType;
	private Integer isDelete;
	private Integer publishStatus;
	private Integer sort;
	private Integer sale;
	private BigDecimal price;
	private BigDecimal promotionPrice;
	private String description;
	private Integer stock;
	private String unit;
	private String keywords;
	private String note;
	private String albumPics;
	private String detailDesc;
	private Integer reducePriceCount;
	private String productCategoryName;
}
