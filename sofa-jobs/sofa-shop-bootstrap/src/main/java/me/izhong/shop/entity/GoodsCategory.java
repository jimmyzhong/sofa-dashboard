package me.izhong.shop.entity;

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
@Table(name = "PRODUCT_CATEGORY")
public class GoodsCategory extends EditableEntity{
	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "PARENT_ID")
    private Long parentId;
    @Column(name = "NAME")
    private String name;
    @Column(name = "LEVEL")
    private Integer level;
    @Column(name = "PRODUCT_COUNT")
    private Integer productCount;
    @Column(name = "PRODUCT_UNIT")
    private String productUnit;
    @Column(name = "SHOW_STATUS")
    private Integer showStatus;
    @Column(name = "SORT")
    private Integer sort;
    @Column(name = "ICON")
    private String icon;
    @Column(name = "KEYWORDS")
    private String keywords;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "PATH")
    private String path;
}
