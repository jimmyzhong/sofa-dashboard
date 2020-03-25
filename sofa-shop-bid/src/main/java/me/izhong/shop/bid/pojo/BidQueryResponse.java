package me.izhong.shop.bid.pojo;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BidQueryResponse extends BaseResponse {

    List<BidQueryItem> bids = new ArrayList<>();
}
