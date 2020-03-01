package me.izhong.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "CART_ITEM")
public class CartItem {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "USER_ID")
    private Long userId;
    @Column(name = "PRODUCT_ID")
    private Long productId;
    @Column(name = "PRODUCT__ATTR_ID")
    private Long productAttributeId; // 购物车存放的也可能是具体某个商品规格
    @Column(name = "QUANTITY")
    private Integer quantity;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "RSV1")
    private String rsv1;
    @Column(name = "RSV2")
    private String rsv2;
    @Column(name = "RSV3")
    private String rsv3;
    @Column(name = "PRODUCT_PIC")
    private String productPic;
    @Column(name = "PRODUCT_NAME")
    private String productName;
    @Column(name = "PRODUCT_SN")
    private String productSn;
    @Column(name = "NICK_NAME")
    private String nickName;
    @Column(name = "CHECKED")
    private Integer checked;
    @Column(name = "IS_DELETE")
    private Integer isDelete;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
