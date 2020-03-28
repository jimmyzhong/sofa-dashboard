package me.izhong.shop.service.impl;

import me.izhong.common.util.DateUtil;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidItem;
import me.izhong.shop.dao.LotsItemDao;
import me.izhong.shop.entity.LotsItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.LotsDao;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.service.ILotsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class LotsService implements ILotsService {

	@Autowired
	private LotsDao lotsDao;
	@Autowired
	private LotsItemDao lotsItemDao;

	@Override
	@Transactional
	public void saveOrUpdate(Lots lots) {
		lotsDao.save(lots);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		lotsDao.deleteById(id);
	}

	@Override
	public Lots findById(Long id) {
		return lotsDao.findById(id).orElseThrow(()-> BusinessException.build("找不到拍卖品" + id));
	}

	@Override
	@Transactional
	public void saveLots(Long bidId, BidDownloadInfo info) {
		Lots lot = findById(bidId);
		lot.setFinalPrice(BigDecimal.valueOf(info.getCurrentPrice()).divide(BigDecimal.valueOf(100)));
		lot.setFinalUser(info.getCurrentUserId());

		List<BidItem> items = info.getBidItems();
		List<LotsItem> lotsItems = new ArrayList<>();
		for (BidItem item : items) {
			LotsItem lotItem = new LotsItem();
			lotItem.setBidTime(DateUtil.convertToLocalDateTime(item.getBidTime()));
			lotItem.setLotsId(lot.getId());
			lotItem.setPrice(item.getPrice());
			lotItem.setUserId(item.getUserId());
			lotItem.setSeqId(item.getSeqId());
			lotsItems.add(lotItem);
		}
		lotsDao.save(lot);
		lotsItemDao.saveAll(lotsItems);
	}
}
