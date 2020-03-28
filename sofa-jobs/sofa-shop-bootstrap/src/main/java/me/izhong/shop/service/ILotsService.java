package me.izhong.shop.service;

import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.shop.entity.Lots;
import org.springframework.transaction.annotation.Transactional;

public interface ILotsService {

    void saveOrUpdate(Lots lots);

	void deleteById(Long id);

	Lots findById(Long id);

    @Transactional
    void saveLots(Long bidId, BidDownloadInfo info);
}
