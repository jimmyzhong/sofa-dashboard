package me.izhong.shop.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemParam {

    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private String rsv1;
    private String rsv2;
    private String rsv3;
    private String productPic;
    private String productName;
    private String productSn;
    private String nickName;
    private Integer checked;
    private Integer isDelete;
}
