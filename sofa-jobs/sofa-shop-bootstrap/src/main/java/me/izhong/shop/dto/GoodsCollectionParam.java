package me.izhong.shop.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsCollectionParam {

    private Long id;
    private Long userId;
    private String nickName;
    private Long productId;
    private String productName;
    private String productPic;
    private String productPrice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
