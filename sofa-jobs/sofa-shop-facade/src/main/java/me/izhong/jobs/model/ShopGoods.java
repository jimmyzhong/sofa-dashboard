package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
    private String productSn;
    private String productName;
    private String productPic;
	private Integer productType;
	private Long productCategoryId;
	private BigDecimal price;
	//促销价格
    private BigDecimal promotionPrice;
	//上架状态: 0->下架;1->上架
	private Integer publishStatus;
	//推荐状态: 0->不推荐;1->推荐
	private Integer recommandStatus;
	//排序
	private Integer sort;
	//销量
	private Integer sale;
	//商品描述
	private String description;
	//库存
	private Integer stock;
	private String unit;
	private String tips;
	private String keywords;
	private String note;
	private List<String> albumPics;
	private String detailDesc;
	//降价次数
	private Integer reducePriceCount;
	private Integer isDelete;
}
