package me.izhong.jobs.model.bid;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class BidResultInfo implements Serializable {

    /**
     * 报价主键
     */
    private Long bidId;

    /**
     * 最高出价 分
     */
    private Long maxPrice;

    /**
     * 出价明细
     */
    private List<BidItems> bidItems;
}
