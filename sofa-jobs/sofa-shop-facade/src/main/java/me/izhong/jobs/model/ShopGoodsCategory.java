package me.izhong.jobs.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopGoodsCategory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
