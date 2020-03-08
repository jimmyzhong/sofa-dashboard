package me.izhong.shop.dto;

import lombok.Data;

@Data
public class GoodsCategoryDTO {

    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer productCount;
    private String productUnit;
    private Integer show_status;
    private Integer sort;
    private String icon;
    private String keywords;
    private String description;
    private String path;
}
