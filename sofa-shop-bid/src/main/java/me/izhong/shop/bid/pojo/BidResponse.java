package me.izhong.shop.bid.pojo;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BidResponse extends BaseResponse {
    private Long price;

    private Long seqId;
}