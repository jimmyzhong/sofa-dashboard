package me.izhong.shop.service.impl;

import com.alibaba.fastjson.JSONArray;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.DateUtil;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidItem;
import me.izhong.shop.consts.LotsStatusEnum;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dao.LotsCategoryDao;
import me.izhong.shop.dao.LotsDao;
import me.izhong.shop.dao.LotsItemDao;
import me.izhong.shop.dao.LotsItemStatsDao;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.LotsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.entity.LotsItem;
import me.izhong.shop.entity.LotsItemStats;
import me.izhong.shop.service.IGoodsService;
import me.izhong.shop.service.ILotsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LotsService implements ILotsService {
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private LotsDao lotsDao;
	@Autowired
	private LotsItemDao lotsItemDao;
	@Autowired
	private LotsItemStatsDao itemStatsDao;
	@Autowired
	private LotsCategoryDao lotsCategoryDao;
	@Autowired
	private IDGeneratorService idGeneratorService;

	@Override
	@Transactional
	public void saveOrUpdate(Lots lots) {
		if (StringUtils.isEmpty(lots.getLotsNo())) {
			String catId = lots.getLotCategoryId().toString();
			if (catId.length() < 4) {
				catId = StringUtils.leftPad(catId, 4, '0'); // make sure at least
			}
			String lotsNo = idGeneratorService.nextID("LOTS" , catId);
			lots.setLotsNo(lotsNo);
		}
		if (lots.getGoodsId() != null) {
			GoodsDTO g = goodsService.findById(lots.getGoodsId());
			if (g != null) {
				lots.setProductPic(g.getProductPic());
				if(g.getAlbumPics() != null && !g.getAlbumPics().isEmpty()) {
					lots.setAlbumPics(JSONArray.toJSONString(g.getAlbumPics()));
				}
			}
		}
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
	public Lots findByLotsNo(String lotsNo) {
		return lotsDao.findFirstByLotsNo(lotsNo);
	}

	@Override
	@Transactional
	public void saveLots(String lotsNo, BidDownloadInfo info) {
		Lots lot = findByLotsNo(lotsNo);
		lot.setNowPrice(BigDecimal.valueOf(info.getCurrentPrice()).divide(BigDecimal.valueOf(100)));
		lot.setFinalUser(info.getCurrentUserId());
		if (info.getIsOver()) {
			lot.setFinalPrice(lot.getNowPrice());
			lot.setOver(true);
		}
		if (lot.getNowPrice().compareTo(lot.getReservePrice())<0) {
			lot.setOver(false);
			lot.setPayStatus(LotsStatusEnum.NOT_DEAL.getType());
		}
		if (lot.getNowPrice().compareTo(lot.getWarningPrice()) >= 0) {
			lot.setOver(false);
			lot.setPayStatus(LotsStatusEnum.DEAL.getType());
		}

		List<BidItem> items = info.getBidItems();
		lot.setBidTimes(items.size());
		lotsDao.save(lot);

		// persistent bids items
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
		lotsItemDao.saveAll(lotsItems);

		// calculate user stats
		Map<Long, List<BidItem>> userMap = items.stream().collect(Collectors.groupingBy(i->i.getUserId()));
		List<LotsItemStats> userStats = new ArrayList<>();
		for (Map.Entry<Long, List<BidItem>> entry: userMap.entrySet()) {
			Long userId = entry.getKey();
			List<BidItem> userBids = entry.getValue();
			LotsItemStats stats = new LotsItemStats();
			stats.setUserId(userId);
			stats.setTimes(userBids.size());
			stats.setAmount(lot.getAddPrice().multiply(BigDecimal.valueOf(stats.getTimes()))
					.setScale(2, BigDecimal.ROUND_HALF_UP));
			stats.setOfferAmount(stats.getAmount().multiply(BigDecimal.valueOf(0.1))); // TODO 返利累计加价10%
			userStats.add(stats);
		}
		itemStatsDao.saveAll(userStats);

		if (lot.getPayStatus() == LotsStatusEnum.DEAL.getType()) {
			// TODO generate an order of AUCTION_REMAINING, 提现final user付尾款

		}
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
	public PageModel<LotsDTO> listLotsOfCategory(PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);

		Specification<Lots> sp = (r, q, cb) -> {
			Predicate p = null;
			if (query.getIsAgent() == null || !query.getIsAgent()) {
				p = cb.equal(r.get("lotCategoryId"), query.getPublicCategoryId());
			} else if (query.getIsAgent() != null && query.getIsAgent()){
				p = cb.notEqual(r.get("lotCategoryId"), query.getPublicCategoryId());
			}

			if (query.getStartTime() != null) {
				p = cb.and(p, cb.greaterThanOrEqualTo(r.get("startTime"),
						query.getStartTime()));
			}
			if (query.getEndTime() != null) {
				p = cb.and(p, cb.lessThan(r.get("startTime"), query.getEndTime()));
			}
			if (query.getIsVip() != null && query.getIsVip()) {
				p = cb.and(p, cb.gt(r.get("userLevel"), 0));
			}
			if (query.getRequiredAuctionMargin() != null) {
				if (query.getRequiredAuctionMargin() == 0) {
					p = cb.and(p, cb.lessThanOrEqualTo(r.get("deposit"), BigDecimal.valueOf(0.001)));
				} else {
					p = cb.and(p, cb.lessThanOrEqualTo(r.get("deposit"),
							BigDecimal.valueOf(query.getRequiredAuctionMargin())));
				}

			}
			return p;
		};
		Page<Lots> page = lotsDao.findAll(sp, pageableReq);
		List<LotsDTO> dto = page.getContent().stream().map(l-> LotsDTO.builder()
				.id(l.getId()).addPrice(l.getAddPrice()).deposit(l.getDeposit())
				.description(l.getDescription()).content(l.getContent()).endTime(l.getEndTime())
				.startTime(l.getStartTime()).finalPrice(l.getFinalPrice()).followCount(l.getFollowCount())
				.maxMemberCount(l.getMaxMemberCount()).lotsNo(l.getLotsNo()).productPic(l.getProductPic())
				.startPrice(l.getStartPrice()).name(l.getName()).bidTimes(l.getBidTimes())
				.build()).collect(Collectors.toList());
		return PageModel.instance(page.getTotalElements(), dto);
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

	@Override
	public PageModel<LotsItem> listBidItems(String lotsNo, PageQueryParamDTO query) {
		Lots lots = findByLotsNo(lotsNo);
		return listBidItems(lots.getId(), query);
	}

	@Override
	public PageModel<LotsItemStats> listStatsItems(String lotsNo, PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "offerAmount");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);
		Page<LotsItemStats> items = itemStatsDao.findByLotsNo(lotsNo, pageableReq);
		return PageModel.instance(items.getTotalElements(), items.getContent());
	}


}
