package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "PRODUCT_ATTR")
@SequenceGenerator(name = "PRODUCT_ATTR_SEQ", sequenceName = "PRODUCT_ATTR_SEQ", allocationSize = 1)
public class GoodsAttributes {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "PRODUCT_ID")
    private Long productId;
    @Column(name = "NAME")
    private String name;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "PROMOTION_PRICE")
    private BigDecimal promotionPrice;
    @Column(name = "ATTRIBUTES")
    private String attributes;
    @Column(name = "STOCK")
    private Integer stock;
}
