package me.izhong.shop.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.DateUtil;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidItem;
import me.izhong.shop.consts.LotsStatusEnum;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.consts.PayMethodEnum;
import me.izhong.shop.dao.*;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.LotsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.*;
import me.izhong.shop.service.IGoodsService;
import me.izhong.shop.service.ILotsService;
import me.izhong.shop.service.IOrderService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LotsService implements ILotsService {
	public static final int AUCTION_PER_RETURN_PERCENTAGE = 5;
	public static final int AUCTION_PER_RETURN_SCORE_PERCENTAGE = 5;

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
	@Autowired
	private IOrderService orderService;
	@Autowired
	private UserDao userDao;
	@Autowired
	private SuppliersService suppliersService;

	@Autowired
	private UserScoreDao userScoreDao;
	@Autowired
	private PayRecordDao payRecordDao;

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

				if (g.getSupplier() != null) {
					Suppliers suppliers = suppliersService.findById(g.getSupplier());
					lots.setSupplier(g.getSupplier());
					lots.setSupplierName(suppliers.getName());
				}

				lots.setDetailDesc(g.getDetailDesc());
			}
		}
		if (lots.getUploaded() == null) {
			lots.setUploaded(0);
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
		Lots l = findByLotsNo(lotsNo);
		boolean isEnd = isEnd(l);

		if (isEnd && l.getPayStatus() != null) {
			log.warn(lotsNo + " already saved " + LotsStatusEnum.getMsgByType(l.getPayStatus()));
			return;
		}

		Lots lot = lotsDao.selectForUpdate(l.getId());
		// check again
		isEnd = isEnd(lot);
		if (isEnd && lot.getPayStatus() != null) {
			log.warn(lotsNo + " already saved " + LotsStatusEnum.getMsgByType(lot.getPayStatus()));
			return;
		}

		if (info == null && isEnd) {
			info = new BidDownloadInfo();
		} else if(!isEnd){
			log.warn("{} bid is not end", lotsNo);
			return;
		}

		if (info.getCurrentPrice() == null) info.setCurrentPrice(0L);

		lot.setNowPrice(BigDecimal.valueOf(info.getCurrentPrice()).divide(BigDecimal.valueOf(100)));
		lot.setFinalUser(info.getCurrentUserId());
		lot.setPayStatus(LotsStatusEnum.NOT_DEAL.getType());
		if (info.getIsOver() != null && info.getIsOver()) {
			lot.setFinalPrice(lot.getNowPrice());
			lot.setOver(true);
			lot.setPayStatus(LotsStatusEnum.DEAL.getType());
		}

		if (lot.getNowPrice().compareTo(lot.getReservePrice())<0) {
			lot.setOver(false);
			lot.setPayStatus(LotsStatusEnum.NOT_DEAL.getType());
		}
		if (lot.getNowPrice().compareTo(lot.getWarningPrice()) >= 0 || lot.getNowPrice().compareTo(lot.getReservePrice()) >=0) {
			lot.setOver(true);
			lot.setPayStatus(LotsStatusEnum.DEAL.getType());
			lot.setFinalPrice(lot.getNowPrice());
		}

		List<BidItem> items = info.getBidItems();
		lot.setBidTimes(items == null ? 0 : items.size());
		lotsDao.save(lot);

		log.info("bid items from cache, size:" + (items == null ? 0 : items.size()));
		Map<Long, User> userInfoMap = new HashMap<>();
		// persistent bids items
		List<LotsItem> lotsItems = new ArrayList<>();
		if (items != null) {
			for (BidItem item : items) {
				LotsItem lotItem = new LotsItem();
				lotItem.setBidTime(DateUtil.convertToLocalDateTime(item.getBidTime()));
				lotItem.setLotsId(lot.getId());
				lotItem.setPrice(item.getPrice());
				lotItem.setUserId(item.getUserId());
				lotItem.setSeqId(item.getSeqId());
				lotItem.setOfferAmount(lot.getAddPrice().multiply(BigDecimal.valueOf(AUCTION_PER_RETURN_PERCENTAGE)).longValue()); // TODO 返利累计加价10%
				lotsItems.add(lotItem);
				User u = userInfoMap.get(item.getUserId());
				if (u == null) {
					u = userDao.findById(item.getUserId()).orElse(new User());
					userInfoMap.put(item.getUserId(), u);
				}
				if (u != null) {
					lotItem.setUserNick(u.getNickName());
					lotItem.setUserAva(u.getAvatar());
				}
			}
			lotsItemDao.saveAll(lotsItems);

			// calculate user stats
			Map<Long, List<BidItem>> userMap = items.stream().collect(Collectors.groupingBy(i->i.getUserId()));
			List<LotsItemStats> userStats = new ArrayList<>();
			List<PayRecord> payRecords = new ArrayList<>();
			for (Map.Entry<Long, List<BidItem>> entry: userMap.entrySet()) {
				Long userId = entry.getKey();
				List<BidItem> userBids = entry.getValue();
				LotsItemStats stats = new LotsItemStats();
				stats.setUserAva(userInfoMap.get(userId).getAvatar());
				stats.setUserNick(userInfoMap.get(userId).getNickName());
				stats.setUserId(userId);
				stats.setTimes(userBids.size());
				stats.setLotsId(lot.getId());
				stats.setAmount(lot.getAddPrice().multiply(BigDecimal.valueOf(stats.getTimes()))
						.setScale(2, BigDecimal.ROUND_HALF_UP));
				stats.setOfferAmount(stats.getAmount().multiply(BigDecimal.valueOf(AUCTION_PER_RETURN_PERCENTAGE))
						.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP)); // TODO 返利累计加价10%
				userStats.add(stats);

				PayRecord record = new PayRecord();
				record.setSysState(0);
				record.setCreateTime(LocalDateTime.now());
				record.setInternalId(lotsNo);
				record.setPayAmount(stats.getOfferAmount());
				record.setTotalAmount(stats.getOfferAmount());
				record.setType(MoneyTypeEnum.RETURN_MONEY.getDescription());
				record.setReceiverId(userId);
				record.setComment("出价返利");
				payRecords.add(record);
			}
			itemStatsDao.saveAll(userStats); // TODO 以获得的奖励，反应在用户余额里
			payRecordDao.saveAll(payRecords);
		}


		boolean isDeal = lot.getPayStatus() == LotsStatusEnum.DEAL.getType();
		// 成交需要拍卖成功者付尾款
		if (isDeal) {
			Long userId = lot.getFinalUser();
			BigDecimal finalPrice = lot.getFinalPrice();
			Order o = orderService.generateAuctionRemainingOrder(userId, lot, finalPrice);
			lot.setOrderSn(o.getOrderSn());
			lotsDao.save(lot);
		}

		List<User> userList = userDao.selectAcutionUsers(MoneyTypeEnum.AUCTION_MARGIN.getType(), lot.getId(),
				OrderStateEnum.PAID.getState());
		// 拍卖失败所有人退保证金; 拍卖成功,其余人退保证金
		userList.stream()
				.filter(u->(isDeal && u.getId() != lot.getFinalUser()) || !isDeal)
				.forEach(u->{
					try {
						orderService.refundMargin(u.getId(), lot);
					}catch (Exception e) {
						log.error("refund margin error for " + u.getId(), e);
					}
				});
	}

	private boolean isEnd(Lots lot) {
		return lot.getEndTime().compareTo(LocalDateTime.now())<0;
	}

	@Override
	public PageModel<LotsDTO> listOfUser(Long userId, PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "CREATE_TIME");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);
		int[] args = new int[5]; // 5 state of lots for now, 0:已参加 1:竞拍中 2:拍中待付款 3:已付款 4:已完成
		if (query.getAuctionFilter() != null && (query.getAuctionFilter() <= 0 || query.getAuctionFilter() > args.length )) {
			throw BusinessException.build("状态未知");
		}
		if (query.getAuctionFilter() != null) {
			args[query.getAuctionFilter() - 1] = 1;
		} else {
			for(int i=0; i< args.length; i++) args[i] = 1;
		}
		LocalDateTime now = LocalDateTime.now();
		Page<Map<String, Object>> page = lotsDao.listOfUser(userId, args[0], args[1],args[2], args[3], args[4],
				now, pageableReq);

		List<LotsDTO> dto = page.getContent().stream().map(m-> {
			Integer type = null;
			if(m.containsKey("order_type")){
				type = Integer.valueOf(m.get("order_type").toString());
			}
			Integer status = null;
			if(m.containsKey("order_status")){
				status = Integer.valueOf(m.get("order_status").toString());;
			}

			Lots l = JSON.parseObject(JSON.toJSON(m).toString(), Lots.class);
			LotsDTO d = buildListDTO(l);
			if (status != null) {
				d.setOrderStatus(OrderStateEnum.getCommentByState(status));
			}
			if (type != null) {
				d.setOrderType(MoneyTypeEnum.getByType(type).name());
			}

			Integer auctionStatus = getAuctionViewStatus(now, type, status, l);
			d.setAuctionViewState(auctionStatus);
			return d;
		}).collect(Collectors.toList());
		return PageModel.instance(page.getTotalElements(), dto);
	}

	/**
	 * 1. 报名没开始
	 * 2. 报名开始了
	 * 3. 拍中没付钱
	 * 4. 拍中付钱了
	 * 5. 结束了 (转拍了、转积分了、转卖了、超时没付尾款、没拍中)
	 * @param now
	 * @param type
	 * @param status
	 * @param l
	 * @return
	 */
	private Integer getAuctionViewStatus(LocalDateTime now, Integer type, Integer status, Lots l) {
		boolean notStarted = l.getStartTime().compareTo(now) > 0;
		boolean ended = l.getEndTime().compareTo(now) <= 0;
		boolean onGoing = (!notStarted) && (!ended);
		boolean done = false;
		if (l.getPayStatus() != null) {
			done = l.getPayStatus() == LotsStatusEnum.EXPIRED.getType()
					|| l.getPayStatus() == LotsStatusEnum.DONE.getType()
					|| l.getPayStatus() == LotsStatusEnum.NOT_DEAL.getType();
		}
		Integer auctionStatus = 0;
		if (type != null && status != null) {
			boolean auctionMargin = type == MoneyTypeEnum.AUCTION_MARGIN.getType()
					&& status == OrderStateEnum.PAID.getState();
			boolean auctionRemain = type == MoneyTypeEnum.AUCTION_REMAIN.getType();
			boolean unpaidRemain = status == OrderStateEnum.WAIT_PAYING_AUCTION_REMAIN.getState();
			boolean paidRemain = status == OrderStateEnum.PAID.getState();
			if (auctionMargin && notStarted) {
				auctionStatus = 1;
			} else if (auctionMargin && onGoing) {
				auctionStatus = 2;
			} else if (auctionRemain && unpaidRemain) {
				auctionStatus = 3;
			} else if (auctionRemain && paidRemain){
				auctionStatus = 4;
			}
			if (done) {
				auctionStatus = 5;
			}
		}
		return auctionStatus;
	}

	@Override
	public PageModel<LotsDTO> listLotsOfCategory(PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);

		if (query.getAgentCategoryId() != null) {
			Optional<LotsCategory> category = lotsCategoryDao.findById(query.getAgentCategoryId());
			if (!category.isPresent()) {
				log.error("cats not exist " + query.getAgentCategoryId());
				throw BusinessException.build("专区编号或密码不正确");
			}
			if (!StringUtils.equalsIgnoreCase(category.get().getPassword(), query.getAgentPassword())) {
				log.error("agent password mismatch, agentID: " + query.getAgentCategoryId());
				throw BusinessException.build("专区编号或密码不正确");
			}
		}
		Specification<Lots> sp = (r, q, cb) -> {
			Predicate p = null;
			if (query.getIsAgent() == null || !query.getIsAgent()) {
				p = cb.equal(r.get("lotCategoryId"), query.getPublicCategoryId());
			} else if (query.getIsAgent() != null && query.getIsAgent()){
				p = cb.notEqual(r.get("lotCategoryId"), query.getPublicCategoryId());
			}
			if (query.getAgentCategoryId() != null) {
				p = cb.equal(r.get("lotCategoryId"), query.getAgentCategoryId());
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
		List<LotsDTO> dto = converToDTO(page);
		return PageModel.instance(page.getTotalElements(), dto);
	}

	private List<LotsDTO> converToDTO(Page<Lots> page) {
		return page.getContent().stream().map(l-> buildListDTO(l)).collect(Collectors.toList());
	}

	private LotsDTO buildListDTO(Lots l) {
		return LotsDTO.builder()
					.id(l.getId()).addPrice(l.getAddPrice()).deposit(l.getDeposit())
					.description(l.getDescription()).content(l.getContent()).endTime(l.getEndTime())
					.startTime(l.getStartTime()).finalPrice(l.getFinalPrice()).followCount(l.getFollowCount())
					.payStatus(l.getPayStatus()).maxMemberCount(l.getMaxMemberCount()).lotsNo(l.getLotsNo())
					.supplier(l.getSupplier()).supplierName(l.getSupplierName())
					.productPic(l.getProductPic()).startPrice(l.getStartPrice()).name(l.getName()).bidTimes(l.getBidTimes())
					.build();
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
		Sort sort = Sort.by(Sort.Direction.DESC, "OFFER_AMOUNT");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);
		Page<LotsItemStats> items = itemStatsDao.findByLotsNo(lotsNo, pageableReq);
		return PageModel.instance(items.getTotalElements(), items.getContent());
	}

	@Override
	@Transactional
	public void markLotsAsUploadedSuccess(Lots lot, String msg) {
		lotsDao.markAsUploadedSuccess(lot.getId(), LocalDateTime.now(),msg);
	}

	@Override
	@Transactional
	public void markLotsAsUploadedFail(Lots lot, String msg) {
		lotsDao.markAsUploadedFail(lot.getId(), LocalDateTime.now(),msg);
	}

	@Override
	@Transactional
	public Lots reCreateLots(String originalLotsNo, Long userId) {
		Lots lot = findByLotsNo(originalLotsNo);
		if (lot.getFinalUser() == null || lot.getFinalUser() != userId || lot.getOrderSn() == null ||
				(lot.getPayStatus() != null && !lot.getPayStatus().equals(LotsStatusEnum.DEAL.getType()))) {
			log.error("unable to re-create lots due to:" +lot.getFinalUser() +
					", userId:" +userId + ",order:"+ lot.getOrderSn());
			throw BusinessException.build("无法为当前用户创建拍卖");
		}
		// TODO need to check order is paid or not
		Order order = orderService.findByOrderNo(lot.getOrderSn());
		if (order == null || order.getStatus() != OrderStateEnum.PAID.getState()) {
			throw BusinessException.build("无法为当前用户创建拍卖,未付款");
		}
		order.setStatus(OrderStateEnum.AUCTION_RENEWED.getState());
		orderService.saveOrUpdate(order);

		lot.setPayStatus(LotsStatusEnum.DONE.getType());
		lot.setComment("已转拍");

		//TODO externalize rule.
		BigDecimal moneyFactor = BigDecimal.ONE.divide(BigDecimal.valueOf(0.925), 6, BigDecimal.ROUND_HALF_UP);
		Integer daysLag = 7;

		Lots newLot = new Lots();
		newLot.setAlbumPics(lot.getAlbumPics());
		newLot.setProductPic(lot.getProductPic());
		newLot.setLotCategoryId(lot.getLotCategoryId());
		newLot.setMaxMemberCount(lot.getMaxMemberCount());
		newLot.setGoodsId(lot.getGoodsId());
		newLot.setName(lot.getName());
		newLot.setDescription(lot.getDescription());
		newLot.setSalePrice(lot.getSalePrice());
		newLot.setContent(lot.getContent());
		newLot.setAuctionFrequency(lot.getAuctionFrequency());
		newLot.setPassword(lot.getPassword());
		newLot.setUserLevel(lot.getUserLevel());
		newLot.setApplyType(lot.getApplyType());
		newLot.setApplyStatus(lot.getApplyStatus());
		newLot.setIsRepublish(lot.getIsRepublish());
		newLot.setIsApply(lot.getIsApply());
		newLot.setPlatformRatio(lot.getPlatformRatio());
		newLot.setRevenueAmount(lot.getRevenueAmount());

		newLot.setLotsNo(idGeneratorService.nextID("LOTS" , newLot.getLotCategoryId().toString()));
		newLot.setAddPrice(lot.getAddPrice().multiply(moneyFactor));
		newLot.setDeposit(lot.getDeposit().multiply(moneyFactor));
		newLot.setWarningPrice(lot.getWarningPrice().multiply(moneyFactor));
		newLot.setReservePrice(lot.getReservePrice().multiply(moneyFactor));
		newLot.setStartPrice(lot.getStartPrice().multiply(moneyFactor));

		newLot.setStartTime(lot.getStartTime().plusDays(daysLag));
		newLot.setEndTime(lot.getEndTime().plusDays(daysLag));

		newLot.setCreateTime(LocalDateTime.now());
		newLot.setCreatedBy(userId);

		newLot = lotsDao.save(newLot);
		lotsDao.save(lot);

		return newLot;
	}

	@Override
	@Transactional
	public Long saleAsScore(String lotsNo, Long userId) {
		Lots lot = findByLotsNo(lotsNo);
		if (lot.getFinalUser() == null || lot.getFinalUser() != userId || lot.getOrderSn() == null ||
				(lot.getPayStatus() != null && !lot.getPayStatus().equals(LotsStatusEnum.DEAL.getType()))) {
			log.error("unable to sale into score due to:" +lot.getFinalUser() +
					", userId:" +userId + ",order:"+ lot.getOrderSn());
			throw BusinessException.build("无法转积分");
		}

		Order order = orderService.findByOrderNo(lot.getOrderSn());
		if (order == null || order.getStatus() != OrderStateEnum.PAID.getState()) {
			throw BusinessException.build("无法为当前用户创建拍卖,未付款");
		}
		order.setStatus(OrderStateEnum.AUCTION_TO_SCORED.getState());
		orderService.saveOrUpdate(order);

		lot.setPayStatus(LotsStatusEnum.DONE.getType());
		lot.setComment("已换积分");

		// TODO externalize score ratio 1:100
		Integer ratio = 100;

		BigDecimal finalPrice = lot.getFinalPrice();
		Long score = finalPrice.multiply(BigDecimal.valueOf(ratio))
				.setScale(2, BigDecimal.ROUND_HALF_UP).longValue();

		UserScore userScore = userScoreDao.selectUserForUpdate(userId);
		userScore.setAvailableScore(userScore.getAvailableScore() + score);
		userScoreDao.save(userScore);

		return score;
	}

	@Override
	public PageModel<LotsDTO> reNewListOfUser(Long userId, PageQueryParamDTO query) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
		Pageable pageableReq = PageRequest.of(Long.valueOf(query.getPageNum()-1).intValue(),
				Long.valueOf(query.getPageSize()).intValue(), sort);

		Integer filter = query.getAuctionFilter();
		Specification<Lots> sp = (r, q, cb) ->{
			Predicate p = cb.equal(r.get("createdBy"), userId);
			if (filter == null) {
				return p;
			}
			LocalDateTime now = LocalDateTime.now();
			// 预展中
			if (filter == 1) {
				p = cb.and(p, cb.greaterThan(r.get("startTime"), now));
			}
			// 竞拍中
			else if (filter == 2) {
				p = cb.and(p, cb.lessThanOrEqualTo(r.get("startTime"), now));
				p = cb.and(p, cb.greaterThan(r.get("endTime"), now));
			}
			// 已成交
			else if (filter == 3) {
				Predicate statusPredicate = cb.or(
						cb.equal(r.get("payStatus"), LotsStatusEnum.DONE.getType()),
						cb.equal(r.get("payStatus"), LotsStatusEnum.DEAL.getType()));
				p = cb.and(p, statusPredicate);
			}
			// 流拍
			else if (filter == 4) {
				p = cb.and(p, cb.equal(r.get("payStatus"), LotsStatusEnum.NOT_DEAL.getType()));
			}
			// 违约
			else if (filter == 5) {
				p = cb.and(p, cb.equal(r.get("payStatus"), LotsStatusEnum.EXPIRED.getType()));
			}
			return p;
		};

		Page<Lots> page = lotsDao.findAll(sp, pageableReq);
		List<LotsDTO> dto = converToDTO(page);
		LocalDateTime now = LocalDateTime.now();
		dto.stream().forEach(d->{
			Integer viewState = 0;
			if (d.getStartTime().compareTo(now) > 0) {
				viewState = 1;
			} else if (d.getStartTime().compareTo(now) <= 0 && d.getEndTime().compareTo(now)>0) {
				viewState = 2;
			}
			if (d.getPayStatus() != null) {
				if (d.getPayStatus() == LotsStatusEnum.DONE.getType() ||
					d.getPayStatus() == LotsStatusEnum.DEAL.getType()) {
					viewState = 3;
				} else if (d.getPayStatus() == LotsStatusEnum.NOT_DEAL.getType()) {
					viewState = 4;
				} else if (d.getPayStatus() == LotsStatusEnum.EXPIRED.getType()) {
					viewState = 5;
				}
			}
			d.setAuctionViewState(viewState);
		});
		return PageModel.instance(page.getTotalElements(), dto);
	}
}
