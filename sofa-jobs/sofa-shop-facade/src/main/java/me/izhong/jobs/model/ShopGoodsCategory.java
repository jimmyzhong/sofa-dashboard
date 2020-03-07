package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShopGoodsCategory {

	private Long id;
	private Long parentId;
	private String name;
	private Integer level;
	private Integer productCount;
	private String productUnit;
	private Integer showStatus;
	private Integer sort;
	private String icon;
	private String keywords;
	private String description;
}
