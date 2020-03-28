package me.izhong.jobs.model.bid;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 缓存的报价信息
 */
@Getter
@Setter
public class BidDownloadInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报价实体id
     */
    private Long bidId;

    /**
     * 报价开始价格，单位分
     */
    private Long startPrice;

    /**
     * 最大价格，单位分
     */
    private Long endPrice;

    /**
     * 每次加价金额，单位分
     */
    private Long stepPrice;

    /**
     * 最后一次报价是否可以超过 endPrice，默认给 true
     */
    private Boolean overPrice;

    /**
     * 报价开始时间
     */
    private Date startTime;

    /**
     * 报价结束时间
     */
    private Date endTime;

    /**
     * 报价创建时间
     */
    private Date createTime;

    /**
     * 当前价格
     */
    private Long currentPrice;

    private Boolean canBid;

    private Boolean isOver;

    /**
     * 最新出价的用户id
     */
    private Long currentUserId;

    /**
     * 最新出价时间
     */
    private Long lastBidTime;

    /**
     * 参与出价的人数
     */
    private Long userSize;

    /**
     * 已经出价的记录数
     */
    private Long bidSize;

    /**
     * 出价明细
     */
    private List<BidItem> bidItems;
}
