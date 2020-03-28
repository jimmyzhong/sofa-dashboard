package me.izhong.jobs.manage;

import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidUploadInfo;
import me.izhong.jobs.model.bid.UserItem;

import java.util.List;

public interface IShopBidActionFacade {

    /**
     * 上送报价信息接口
     * @param bid
     * @return true 上送成功
     */
    Boolean uploadBid(BidUploadInfo bid);

    Boolean stopBid(Long bidId);

    Boolean startBid(Long bidId);

    /**
     * 下载报价信息
     * @param bidId 报价的主键
     * @param startIndex 开始id ，从1开始， 默认填1，如果不下载报价明细，只看报价信息，这个填null
     * @param max 下载数量，防止超过报文大小，开始先填null 下载全部
     */
    BidDownloadInfo downloadBid(Long bidId, Long startIndex, Long max);

    /**
     * 中途参加报价的，增加报价人信息
     * @param bidId
     * @param users
     * @return
     */
    Boolean addBidUsers(Long bidId , List<UserItem> users);
}
