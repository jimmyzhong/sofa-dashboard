package me.izhong.shop.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import me.izhong.shop.entity.GoodsAttributes;

@Data
@Builder
public class GoodsDTO {
    @Tolerate
    public GoodsDTO() {

    }
    private Long id;
    private Long productCategoryId;
    private Long productAttributeCategoryId;
    private String name;
    private String pic;
    private String productSn;
    private Integer productType;
    private Integer isDelete;
    private Integer publishStatus;
    private Integer recommandStatus;
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
    private String reducePriceCount;
    private String productCategoryName;
    private List<GoodsAttributes> attributes;
}
