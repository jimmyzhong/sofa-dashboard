package me.izhong.shop.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class SubmitOrderRequest{
    String orderNo;
    List<Long> cartItems;
    Long addressId;

    Long productId;
    Long productAttrId;
    Integer quantity;

    Long auctionId;
}
