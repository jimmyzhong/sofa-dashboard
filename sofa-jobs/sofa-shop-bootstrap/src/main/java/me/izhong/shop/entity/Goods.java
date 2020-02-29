package me.izhong.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "PRODUCT")
@SequenceGenerator(name = "PRODUCT_SEQ", sequenceName = "PRODUCT_SEQ", allocationSize = 1)
public class Goods {

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
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
