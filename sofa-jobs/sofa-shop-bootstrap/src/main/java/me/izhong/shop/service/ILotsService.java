package me.izhong.shop.service;

import me.izhong.common.domain.PageModel;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.shop.dto.LotsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.entity.LotsItem;
import org.springframework.transaction.annotation.Transactional;

public interface ILotsService {

    void saveOrUpdate(Lots lots);

	void deleteById(Long id);

	Lots findById(Long id);

    Lots findByLotsNo(String lotsNo);

    @Transactional
    void saveLots(Long bidId, BidDownloadInfo info);

    PageModel<Lots> listOfUser(Long id, PageQueryParamDTO query);

    PageModel<LotsDTO> listLotsOfCategory(PageQueryParamDTO query);

    PageModel<LotsCategory> listCategory(PageQueryParamDTO query);

    PageModel<LotsItem> listBidItems(Long auctionId, PageQueryParamDTO query);

    PageModel<LotsItem> listBidItems(String lotsNo, PageQueryParamDTO query);
}
