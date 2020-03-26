package me.izhong.jobs.manage;

import me.izhong.jobs.model.bid.BidResultInfo;
import me.izhong.jobs.model.bid.BidUploadInfo;

public interface IShopBidActionFacade {

    /**
     * 上送报价信息接口
     * @param bid
     * @return true 上送成功
     */
    Boolean uploadBid(BidUploadInfo bid);

    /**
     * 下载报价信息
     * @param bidId 报价的主键
     * @param startIndex 开始id ，从1开始， 默认填1
     * @param max 下载数量，防止超过报文大小，开始先填null 下载全部
     */
    BidResultInfo downloadBid(Long bidId, Long startIndex, Long max);

}
