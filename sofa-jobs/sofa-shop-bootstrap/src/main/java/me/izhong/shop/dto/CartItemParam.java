package me.izhong.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemParam {

    private Long id;
    private Long userId;
    private Long productId;
    private Long productAttrId;
    private Integer quantity;
    private BigDecimal price; // unit price
    private String rsv1;
    private String rsv2;
    private String rsv3;
    private String productPic;
    private String productName;
    private String productSn;
    private Integer checked;
    private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
