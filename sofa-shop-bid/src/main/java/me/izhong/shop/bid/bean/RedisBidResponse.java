package me.izhong.shop.bid.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisBidResponse {

    private Long allow;

    private Long seqId;

    private Long price;

    public boolean isSuccess(){
        return allow >= 0;
    }

}
