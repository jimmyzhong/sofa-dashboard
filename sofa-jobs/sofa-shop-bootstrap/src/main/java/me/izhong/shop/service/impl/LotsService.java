package me.izhong.shop.service.impl;

import me.izhong.common.domain.PageModel;
import me.izhong.common.util.DateUtil;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidItem;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dao.LotsCategoryDao;
import me.izhong.shop.dao.LotsItemDao;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.entity.LotsItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.LotsDao;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.service.ILotsService;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class LotsService implements ILotsService {

	@Autowired
	private LotsDao lotsDao;
	@Autowired
	private LotsItemDao lotsItemDao;
	@Autowired
	private LotsCategoryDao lotsCategoryDao;

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
		lot.setNowPrice(BigDecimal.valueOf(info.getCurrentPrice()).divide(BigDecimal.valueOf(100)));
		lot.setFinalUser(info.getCurrentUserId());
		if (info.getIsOver()) {
			lot.setFinalPrice(lot.getNowPrice());
			lot.setOver(true);
		}

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

	@Override
	public PageModel<Lots> listOfUser(Long userId, PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "CREATE_TIME");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);

		Page<Lots> page = lotsDao.findAllByUser(userId, MoneyTypeEnum.AUCTION_MARGIN.getType(),OrderStateEnum.PAID.getState(), pageableReq);

		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

	@Override
	public PageModel<Lots> listLotsOfCategory(PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);

		Specification<Lots> sp = (r, q, cb) -> {
			Predicate p = cb.equal(r.get("lotCategoryId"), query.getLotsCategoryId());
			// TODO add condition
			return p;
		};
		Page<Lots> page = lotsDao.findAll(sp, pageableReq);
		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

	@Override
	public PageModel<LotsCategory> listCategory(PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.ASC, "name");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);

		Page<LotsCategory> page = lotsCategoryDao.findAll(pageableReq);
		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

	@Override
	public PageModel<LotsItem> listBidItems(Long auctionId, PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "bidTime");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);

		Specification<LotsItem> sp = (r, q, cb) -> {
			Predicate p = cb.equal(r.get("lotsId"), auctionId);
			if (query.getUserId() != null) {
				p = cb.and(p, cb.equal(r.get("userId"), query.getUserId()));
			}
			return p;
		};
		Page<LotsItem> bidItems = lotsItemDao.findAll(sp, pageableReq);
		return PageModel.instance(bidItems.getTotalElements(), bidItems.getContent());
	}
}
