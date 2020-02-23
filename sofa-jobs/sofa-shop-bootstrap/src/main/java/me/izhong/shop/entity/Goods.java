package me.izhong.shop.entity;

import java.math.BigDecimal;

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
@Table(name = "PRODUCT")
public class Goods extends EditableEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "PRODUCT_CATEGORY_ID")
    private Long productCategoryId;
    @Column(name = "PRODUCT_ATTRIBUTE_CATEGORY_ID")
    private Long productAttributeCategoryId;
    @Column(name = "NAME")
    private String name;
    @Column(name = "PIC")
    private String pic;
    @Column(name = "PRODUCT_SN")
    private String productSn;
    @Column(name = "PRODUCT_TYPE")
    private Integer productType;
    @Column(name = "IS_DELETE")
    private Integer isDelete;
    @Column(name = "PUBLISH_STATUS")
    private Integer publishStatus;
    @Column(name = "RECOMMAND_STATUS")
    private Integer recommandStatus;
    @Column(name = "SORT")
    private Integer sort;
    @Column(name = "SALE")
    private Integer sale;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "PROMOTION_PRICE")
    private BigDecimal promotionPrice;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "STOCK")
    private Integer stock;
    @Column(name = "UNIT")
    private String unit;
    @Column(name = "KEYWORDS")
    private String keywords;
    @Column(name = "NOTE")
    private String note;
    @Column(name = "ALBUM_PICS")
    private String albumPics;
    @Column(name = "DETAIL_DESC")
    private String detailDesc;
    @Column(name = "REDUCE_PRICE_COUNT")
    private String reducePriceCount;
    @Column(name = "PRODUCT_CATEGORY_NAME")
    private String productCategoryName;
}
