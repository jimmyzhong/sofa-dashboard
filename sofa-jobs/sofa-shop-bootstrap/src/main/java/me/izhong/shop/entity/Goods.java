package me.izhong.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static me.izhong.shop.consts.ProductTypeEnum.NORMAL;

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
    @Column(name = "PRODUCT_SN")
    private String productSn;
    @Column(name = "PRODUCT_NAME")
    private String productName;
    @Column(name = "PRODUCT_PIC")
    private String productPic;
    @Column(name = "PRODUCT_TYPE")
    private Integer productType = NORMAL.getType();
    @Column(name = "PRODUCT_CATEGORY_ID")
    private Long productCategoryId;
    @Column(name = "PRODUCT_CAT_PATH")
    private String categoryPath;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "ORIGIN_PRICE")
    private BigDecimal originalPrice;
    @Column(name = "PROMOTION_PRICE")
    private BigDecimal promotionPrice;
    @Column(name = "PUBLISH_STATUS")
    private Integer publishStatus;
    @Column(name = "RECOMMAND_STATUS")
    private Integer recommandStatus;
    @Column(name = "SORT")
    private Integer sort;
    @Column(name = "SALE")
    private Integer sale;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "STOCK")
    private Integer stock;
    @Column(name = "UNIT")
    private String unit;
    @Column(name = "TIPS")
    private String tips;
    @Column(name = "KEYWORDS")
    private String keywords;
    @Column(name = "NOTE")
    private String note;
    @Column(name = "ALBUM_PICS", length = 1024)
    private String albumPics;
    @Column(name = "DETAIL_DESC")
    private String detailDesc;
    @Column(name = "REDUCE_PRICE_COUNT")
    private String reducePriceCount;
    @Column(name = "IS_DELETE")
    private Integer isDelete;
    @Column(name = "ON_INDEX_PAGE")
    private Boolean onIndexPage;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
    @Column(name = "CREATED_BY")
    private Long createdBy;
    @Column(name = "SCORE_REDEEM", columnDefinition = "int(1) default 1")
    private Integer scoreRedeem; //1 可以积分兑换, 0 不能积分兑换
    @Column(name="RESALE_REDUCE_FACTOR")
    private Double resaleReduceValue;
    @Column(name = "RESALE_TIME_STEP")
    private Double resaleTimeStep;
    @Column(name = "RESALE_LIMIT")
    private Double resaleLimit;
    @Column(name = "SUPPLIER")
    private Long supplier;
}
