package me.izhong.shop.service;

import me.izhong.common.domain.PageModel;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.shop.dto.LotsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.entity.LotsItem;
import me.izhong.shop.entity.LotsItemStats;
import org.springframework.transaction.annotation.Transactional;

public interface ILotsService {

    void saveOrUpdate(Lots lots);

	void deleteById(Long id);

	Lots findById(Long id);

    Lots findByLotsNo(String lotsNo);

    /**
     * 拍卖结束时，统计保存拍卖信息
     * @param lotsNo
     * @param info
     */
    void saveLots(String lotsNo, BidDownloadInfo info);

    PageModel<LotsDTO> listOfUser(Long id, PageQueryParamDTO query);

    PageModel<LotsDTO> listLotsOfCategory(PageQueryParamDTO query);

    PageModel<LotsCategory> listCategory(PageQueryParamDTO query);

    PageModel<LotsItem> listBidItems(Long auctionId, PageQueryParamDTO query);

    PageModel<LotsItem> listBidItems(String lotsNo, PageQueryParamDTO query);

    PageModel<LotsItemStats> listStatsItems(String lotsNo, PageQueryParamDTO query);

    void markLotsAsUploadedSuccess(Lots lots, String msg);
    void markLotsAsUploadedFail(Lots lots, String msg);

    /**
     * 转拍
     * @param originalLotsNo
     * @param userId
     * @return
     */
    Lots reCreateLots(String originalLotsNo, Long userId);

    @Transactional
    Long saleAsScore(String lotsNo, Long userId);
}
