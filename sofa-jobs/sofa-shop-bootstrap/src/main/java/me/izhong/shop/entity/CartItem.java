package me.izhong.shop.entity;

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
    @Column(name = "PRODUCT_ATTR_ID")
    private Long productAttributeId; // 购物车存放的也可能是具体某个商品规格
    @Column(name = "QUANTITY")
    private Integer quantity;
    @Column(name = "CHECKED")
    private Integer checked;
    @Column(name = "IS_DELETE")
    private Integer isDelete;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
