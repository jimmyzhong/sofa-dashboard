package me.izhong.shop.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "TX_ORDER_ITEM")
@SequenceGenerator(name = "ORDER_ITEM_SEQ", sequenceName = "ORDER_ITEM_SEQ", allocationSize = 1)
public class OrderItem {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "USER_ID")
    private Long userId;
    @Column(name = "PRODUCT_ID")
    private Long productId;
    @Column(name = "PRODUCT_PIC")
    private String productPic;
    @Column(name = "PRODUCT__ATTR_ID")
    private Long productAttributeId;
    @Column(name = "NAME")
    private String name;
//    @Column(name = "ORDER_ID")
    private Long orderId;
    @Column(name = "QUANTITY")
    private Integer quantity;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "UNIT_PRICE")
    private BigDecimal unitPrice;
    @Column(name = "PAID_PRICE")
    private BigDecimal paidPrice;
    @Column(name = "SCORE_REDEEM")
    private Integer scoreRedeem;
}
