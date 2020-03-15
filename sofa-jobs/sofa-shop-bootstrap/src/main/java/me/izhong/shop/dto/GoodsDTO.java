package me.izhong.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private String productSn;
    private String productName;
    private String productPic;
	private Integer productType;
	private Long productCategoryId;
	private String productCategoryPath;
	private BigDecimal price;
	private BigDecimal originalPrice;
    private BigDecimal promotionPrice;
	private Integer publishStatus;
	private Integer recommandStatus;
	private Integer sort;
	private Integer sale;
	private String description;
	private Integer stock;
	private String unit;
	private String tips;
	private String keywords;
	private String note;
	private List<String> albumPics;
	private String detailDesc;
	private Integer reducePriceCount;
	private Integer isDelete;
	private LocalDateTime nextPriceTime; // 寄售商品下一次降价时间
    private List<GoodsAttributes> attributes;
}
