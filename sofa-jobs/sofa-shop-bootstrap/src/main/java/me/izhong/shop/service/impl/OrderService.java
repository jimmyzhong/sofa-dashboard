package me.izhong.shop.service.impl;

import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.dto.OrderDeliveryParam;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.annotation.NeedOptimisticLockRetry;
import me.izhong.shop.consts.*;
import me.izhong.shop.dao.*;
import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.dto.order.OrderDTO;
import me.izhong.shop.dto.order.OrderFullDTO;
import me.izhong.shop.entity.*;
import me.izhong.shop.service.IGoodsService;
import me.izhong.shop.service.ILotsService;
import me.izhong.shop.service.IOrderService;
import me.izhong.shop.service.IReceiveAddressService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.izhong.shop.consts.MoneyTypeEnum.*;
import static me.izhong.shop.consts.OrderStateEnum.*;

@Slf4j
@Service
public class OrderService implements IOrderService {

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private OrderItemDao orderItemDao;
	@Autowired
	private PayRecordDao payRecordDao;
	@Autowired
	private UserReceiveAddressDao userReceiveAddressDao;
	@Autowired
	private IReceiveAddressService addressService;
	@Autowired
	private CartItemService cartItemService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private GoodsDao goodsDao;
	@Autowired
	private GoodsStoreDao storeDao;
	@Autowired
	private UserService userService;
	@Autowired
	private UserMoneyDao userMoneyDao;
	@Autowired
	private UserScoreDao userScoreDao;

	@Value("${order.expire.time}")
	private Long orderExpireMinutes;
	@Value("${score.pay.rate}")
	private Double scorePayRate;
	@Value("${score.return.rate}")
	private Double scoreReturnRate;

	@PostConstruct
	public void setUp() {
		ScheduledExecutorService orderStatusUpdater = Executors.newSingleThreadScheduledExecutor(
				new ThreadFactoryBuilder()
				.setNameFormat("order-status-updater").build());
		orderStatusUpdater.scheduleAtFixedRate(() -> {
			try {
				//updateExpiredOrders();
			}catch (Throwable throwable) {
				log.error("order update expired status error", throwable);
			}
		}, 1, 2, TimeUnit.MINUTES);
	}

	@Override
	@Transactional
	@NeedOptimisticLockRetry
	public void updateExpiredOrders() {
		Long start = System.currentTimeMillis();
		try {
			List<Order> unpaidOrders = orderDao.findAllByStatusAndCreateTimeBeforeOrderByOrderSnDesc(
					WAIT_PAYING.getState(), LocalDateTime.now().minusMinutes(orderExpireMinutes));
			log.info("find orders : " + unpaidOrders.size());
			if (!unpaidOrders.isEmpty()) {
				for (Order order : unpaidOrders) {
					restoreStock(order);
				}
				orderDao.updateOrderStatus(unpaidOrders.stream().map(Order::getId).collect(Collectors.toList()),
						EXPIRED.getState());
			}
		} finally {
			log.info("task finish time: " + (System.currentTimeMillis() - start));
		}
	}

	private void restoreStock(Order order) {
		List<OrderItem> items = orderItemDao.findAllByOrOrderIdAndUserId(order.getId(), order.getUserId());
		items.stream().forEach(i->{
			GoodsStore store = storeDao.findByProductIdAndProductAttrId(i.getProductId(),
					i.getProductAttributeId());
			store.setPreStore(store.getPreStore() + i.getQuantity());
			storeDao.save(store);
		});
	}

	@Override
	@Transactional
	public Order saveOrUpdate(Order order) {
		return orderDao.save(order);
	}

	@Override
	public Order findById(Long orderId) {
		return orderDao.findById(orderId).orElseThrow(() -> new RuntimeException("unable to find order by " + orderId));
	}

	@Override
	public OrderFullDTO findFullOrderByOrderNo(String orderNo) {
		Order order = findByOrderNo(orderNo);
		List<OrderItem> items = orderItemDao.findAllByOrOrderIdAndUserId(order.getId(), order.getUserId());

		checkOrderExpired(order);

		OrderFullDTO dto = new OrderFullDTO();
		BeanUtils.copyProperties(order, dto);
		dto.setStatusComment(getCommentByState(dto.getStatus()));
		dto.setItems(items);
		return dto;
	}

	private Order checkOrderExpired(Order order) {
		if (Integer.valueOf(WAIT_PAYING.getState()).equals(order.getStatus()) &&
				LocalDateTime.now().isAfter(order.getCreateTime().plusMinutes(orderExpireMinutes))) {
			order.setStatus(EXPIRED.getState());
		}
		return order;
	}

	@Override
	public void deleteById(Long goodsId) {
		orderDao.deleteById(goodsId);
	}

	@Override
	public void updateOrderStatusByIds(Integer orderStatus, List<Long> ids) {
		orderDao.updateOrderStatus(ids, orderStatus);
	}

	@Override
	public void updateReceiverInfoById(ShopReceiverInfo shopReceiverInfo) {
		orderDao.updateReceiverInfo(shopReceiverInfo);
	}

	@Override
	public void updateNoteById(Long id, String note) {
		orderDao.updateNote(note, id);
	}

	@Override
	public Order findByOrderNo(String orderNo) {
		return orderDao.findFirstByOrderSn(orderNo);
	}

	@Override
	@Transactional
	@NeedOptimisticLockRetry
	public void updatePayInfo(Order order, String externalOrderNo, String payMethod,
							  String payType, BigDecimal payAmount, BigDecimal totalAmount,
							  String state, String comment) {
		// 付款记录
		PayRecord record = payRecordDao.findFirstByInternalIdAndTypeAndPayerId(order.getOrderSn(), payType, order.getUserId());
		if (record == null) {
			record = new PayRecord();
			record.setSysState(0);
			record.setCreateTime(LocalDateTime.now());
		}
		record.setInternalId(order.getOrderSn());
		record.setExternalId(externalOrderNo);
		record.setPayAmount(payAmount);
		record.setTotalAmount(totalAmount);
		record.setPayMethod(payMethod);
		record.setType(payType);
		record.setPayerId(order.getUserId());
		record.setPayState(state);

		comment = StringUtils.substring(comment,0,200);
		record.setComment(comment);

		order.setPayType(PayMethodEnum.valueOf(payMethod).getCode());
		order.setPayTradeNo(externalOrderNo);
		order.setPayAmount(payAmount);

		if (PayStatusEnum.SUCCESS.name().equals(state) && (order.getStatus() == WAIT_PAYING.getState()
				|| order.getStatus() == EXPIRED.getState())) {
			order.setStatus(PAID.getState());
			record.setSysState(1);

			if (StringUtils.equals(payType, NORMAL_GOODS.getDescription()) ||
					StringUtils.equals(payType, RESALE_GOODS.getDescription())) {
				// 更新库存、销售量信息
				List<OrderItem> items = orderItemDao.findAllByOrOrderIdAndUserId(order.getId(), order.getUserId());
				Set<Goods> goodsSetToUpdateStockAndSale = new HashSet<>();
				for (OrderItem item : items) {
					GoodsStore store = storeDao.findByProductIdAndProductAttrId(item.getProductId(), item.getProductAttributeId());
					store.setStore(store.getStore() - item.getQuantity());
					storeDao.save(store);
					goodsDao.findById(item.getProductId()).ifPresent(goods -> {
						goods.setStock(goods.getStock() - item.getQuantity());
						goods.setSale((goods.getSale() == null ? 0 : goods.getSale()) + item.getQuantity());
						goodsSetToUpdateStockAndSale.add(goods);
					});
				}
				goodsDao.saveAll(goodsSetToUpdateStockAndSale);

				// 返现记录
				User user = userService.findById(order.getUserId());
				if (user.getInviteUserId() != null) {
					recordMoneyReturn(order, user.getId(), user.getInviteUserId(), 0.03, null);  //TODO externalize
				}
				if (user.getInviteUserId2() != null) {
					recordMoneyReturn(order, user.getId(), user.getInviteUserId2(), 0.02, null);  //TODO externalize
				}
				// 付款成功,此处应该有积分奖励
				if (!payMethod.equalsIgnoreCase(PayMethodEnum.SCORE.name())) {
					recordScoreReturn(order);
				}
				// 寄售商品付款到寄售人
				if (StringUtils.equals(payType, RESALE_GOODS.getDescription())) {
					record.setReceiverId(order.getResaleUser());
					record.setSysState(0); // 需要后台处理
				}
			}

			// 充值金额充值账户余额,直接到账
			if (StringUtils.equals(payType, DEPOSIT_MONEY.getDescription())) {
				recordMoney(order, null, order.getUserId(), 1.0, 1, true, DEPOSIT_MONEY, null);
			}
		}

		payRecordDao.save(record);
		orderDao.save(order);
	}

	@Override
	@Transactional
	public boolean transferMoney(User user, String orderNo, Order order, AliPayService aliPayService) {
		UserMoney userMoney = userMoneyDao.selectUserForUpdate(user.getId());
		if (userMoney == null) {
			throw BusinessException.build("用户余额不存在,请联系管理员");
		}

		BigDecimal amount = order.getTotalAmount();
		if (userMoney.getAvailableAmount().compareTo(amount) < 0) {
			throw BusinessException.build("可用余额不足");
		}

		try {
			AlipayFundTransUniTransferResponse response = aliPayService.
					transfer(orderNo, order.getTotalAmount(), user.getAlipayAccount(), user.getAlipayName());

			if (!response.isSuccess()) {
				log.error("提现失败 " + response.getBody());
				throw BusinessException.build("提现失败");
			}

			if ("SUCCESS".equalsIgnoreCase(response.getStatus())) {
				order.setStatus(FINISHED.getState());
				userMoney.setAvailableAmount(userMoney.getAvailableAmount().subtract(amount));
				order = orderDao.save(order);
				recordMoney(order, user.getId(), null, 1.0, 1, false , WITHDRAW_MONEY, userMoney.getAvailableAmount());
				userMoneyDao.save(userMoney);
				return true;
			}
		}catch (Exception e) {
			log.error("transfer error",e);
		}

		return false;
	}

	private void recordMoneyReturn(Order order, Long payerId, Long receiverId, Double returnFactor, BigDecimal accountAmount) {
		recordMoney(order, payerId, receiverId, returnFactor, 0, false, MoneyTypeEnum.RETURN_MONEY, accountAmount);
	}

	/**
	 * 记录支付记录
	 * @param order
	 * @param payerId
	 * @param receiverId
	 * @param returnFactor
	 * @param sysState
	 * @param updateUserMoney
	 * @param type
	 * @param accountAmount 账户当前余额
	 */
	private void recordMoney(Order order, Long payerId, Long receiverId, Double returnFactor, int sysState,
							 boolean updateUserMoney, MoneyTypeEnum type, BigDecimal accountAmount) {
		BigDecimal amount = order.getTotalAmount().multiply(BigDecimal.valueOf(returnFactor));
		if (amount.compareTo(BigDecimal.ZERO) == 0) {
			log.warn("log money amount 0. ignore");
			return;
		}
		PayRecord moneyReturn = new PayRecord();
		moneyReturn.setType(type.getDescription());
		moneyReturn.setCreateTime(LocalDateTime.now());
		moneyReturn.setPayerId(payerId);
		moneyReturn.setReceiverId(receiverId);
		moneyReturn.setTotalAmount(accountAmount);
		moneyReturn.setPayAmount(amount);
		moneyReturn.setInternalId(order.getOrderSn());
		moneyReturn.setSysState(sysState);

		if (updateUserMoney) {
			UserMoney userMoney = userMoneyDao.selectUserForUpdate(order.getUserId());
			if (userMoney == null) {
				userMoney = new UserMoney();
				userMoney.setUserId(order.getUserId());
				userMoney.setAvailableAmount(moneyReturn.getPayAmount());
			} else {
				userMoney.setAvailableAmount(userMoney.getAvailableAmount().add(moneyReturn.getPayAmount()));
			}
			if (type == DEPOSIT_MONEY) {
				userMoney.setMoneyDepositAmount(userMoney.getMoneyDepositAmount()
						.add(moneyReturn.getPayAmount()));
				moneyReturn.setTotalAmount(userMoney.getAvailableAmount());
			} else if (type == RETURN_MONEY) {
				userMoney.setMoneyReturnAmount(userMoney.getMoneyReturnAmount()
						.add(moneyReturn.getPayAmount()));
				moneyReturn.setTotalAmount(userMoney.getAvailableAmount());
			} else if (type == RESALE_GOODS) {
				userMoney.setMoneySaleAmount(userMoney.getMoneySaleAmount()
						.add(moneyReturn.getPayAmount()));
				moneyReturn.setTotalAmount(userMoney.getAvailableAmount());
			}
			userMoneyDao.save(userMoney);
		}
		payRecordDao.save(moneyReturn);
	}

	private void recordScoreReturn(Order order) {
		PayRecord scoreReturn = new PayRecord();
		scoreReturn.setType(MoneyTypeEnum.RETURN_SCORE.getDescription());
		scoreReturn.setCreateTime(LocalDateTime.now());
		scoreReturn.setReceiverId(order.getUserId());
		scoreReturn.setPayAmount(order.getTotalAmount());
		scoreReturn.setInternalId(order.getOrderSn());
		scoreReturn.setSysState(1);

		UserScore userScore = userScoreDao.findByUserId(order.getUserId());
		Long score = BigDecimal.valueOf(scoreReturn.getPayAmount().doubleValue() * scoreReturnRate).longValue();
		if (userScore == null) {
			userScore = new UserScore();
			userScore.setUserId(order.getUserId());
			userScore.setAvailableScore(score);
		} else {
			userScore.setAvailableScore(userScore.getAvailableScore() + score);
		}
		scoreReturn.setTotalAmount(BigDecimal.valueOf(userScore.getAvailableScore()));
		userScoreDao.save(userScore);
		payRecordDao.save(scoreReturn);
	}


	/**
	 * 直接买
	 * 1.创建订单和订单明细
	 * 2.清空购物车
	 * 3.减库存
	 */
	@Override
	@Transactional
	@NeedOptimisticLockRetry
	public Order submit(Long userId, Long addressId, Long productId, Long productAttrId, Integer quantity) {
		UserReceiveAddress address = getUserReceiveAddress(userId, addressId);
		if (address == null) {
			throw BusinessException.build("地址不存在");
		}

		GoodsDTO goods = goodsService.findGoodsWithAttrById(productId, productAttrId);
		if (goods == null) {
			throw BusinessException.build("商品不存在");
		}

		// 自己能否购买自己的寄售商品 ？
		if (goods.getProductType() != null && goods.getProductType().equals(ProductTypeEnum.RESALE.getType())) {
			if (goods.getCreatedBy() != null && goods.getCreatedBy().equals(userId)) {
				throw BusinessException.build("自己不能购买自己的寄售商品");
			}
		}

		// 预减库存
		GoodsStore store = storeDao.findByProductIdAndProductAttrId(productId, productAttrId);
		if (store==null || store.getPreStore() < quantity || store.getStore() < quantity) {
			throw BusinessException.build("库存不足");
		}
		store.setPreStore(store.getPreStore() - quantity);
		storeDao.save(store);

		Order order = new Order();
		if (goods.getProductType() == ProductTypeEnum.NORMAL.getType()) {
			order.setOrderType(NORMAL_GOODS.getType());
		} else if (goods.getProductType() == ProductTypeEnum.RESALE.getType()) {
			order.setOrderType(RESALE_GOODS.getType());
			order.setResaleUser(goods.getCreatedBy());
		}
		setReceiverInfoOfOrder(address, order);

		OrderItem item = new OrderItem();
		item.setProductId(goods.getId());
		item.setQuantity(quantity);
		item.setUserId(userId);
		item.setProductPic(goods.getProductPic());
		item.setScoreRedeem(goods.getScoreRedeem());
		item.setUnitPrice(goods.getPromotionPrice()!=null ? goods.getPromotionPrice() : goods.getPrice());
		if (goods.getAttributes()!=null && !goods.getAttributes().isEmpty()){
			GoodsAttributes attributes = goods.getAttributes().get(0);
			item.setName(attributes.getName());
			item.setPrice(getSalePrice(attributes));
			item.setProductAttributeId(attributes.getId());
		} else {
			item.setName(goods.getProductName());
			item.setPrice(getSalePrice(goods));
		}
		order.setTotalAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

		order.setUserId(userId);
		order.setCount(1);
		order.setOrderSn(generateOrderNo());
		order.setDescription(item.getName() +":x"+ item.getQuantity());
		order.setSubject(order.getOrderSn() + ", 共有品类" + order.getCount());
		order.setStatus(WAIT_PAYING.getState());
		order.setCreateTime(LocalDateTime.now());
		order.setUnitPrice(item.getUnitPrice());
		order.setProductPic(item.getProductPic());

		order = orderDao.save(order);
		item.setOrderId(order.getId());
		orderItemDao.save(item);

        return order;
	}

	/**
	 * 购物车
	 * @param userId
	 * @param addressId
	 * @param cartIds
	 * @return
	 */
	@Override
	@Transactional
	public Order submit(Long userId, Long addressId, List<Long> cartIds) {
		UserReceiveAddress address = getUserReceiveAddress(userId, addressId);
		if (address == null) {
			throw BusinessException.build("地址不存在");
		}
		// TODO 校验库存，能否购买，预减库存
		Order order = new Order();
		order.setOrderType(NORMAL_GOODS.getType());
		setReceiverInfoOfOrder(address, order);

		List<CartItemParam> carts = cartItemService.list(cartIds);
		if (carts.isEmpty()) {
			throw BusinessException.build("找不到订单内容");
		}
		List<OrderItem> orderItems = generateOrderDetailFromCart(userId, carts, order);

		order.setOrderSn(generateOrderNo());
		order.setStatus(WAIT_PAYING.getState());
		order.setCreateTime(LocalDateTime.now());

		order = orderDao.save(order);
		Long orderId = order.getId();
		orderItems.forEach(o->o.setOrderId(orderId));
		orderItemDao.saveAll(orderItems);
		cartItemService.delete(userId,
				carts.stream().map(CartItemParam::getId).collect(Collectors.toList()));
		return order;
	}

	@Override
	@Transactional
	public Order confirm(Long userId, String orderNo) {
		Order order = orderDao.findFirstByOrderSn(orderNo);
		if (order.getStatus() < PAID.getState()) {
			throw BusinessException.build("订单为支付");
		}
		order.setStatus(CONFIRMED.getState());
		orderDao.save(order);
		return order;
	}

	@Override
	@Transactional
	@NeedOptimisticLockRetry
	public Order cancel(Long currentUserId, String orderNo) {
		Order order = orderDao.findFirstByOrderSn(orderNo);
		order.setStatus(CANCELED.getState());
		restoreStock(order);
		orderDao.save(order);
		return order;
	}

	@Override
	@Transactional
	public void delete(Long currentUserId, String orderNo) {
		Order order = orderDao.findFirstByOrderSn(orderNo);
		order.setIsDelete(1);
		orderDao.save(order);
	}

	@Override
	public Map<String, Integer> getCountOfStatus(Long currentUserId, List<MoneyTypeEnum> types, List<OrderStateEnum> states) {
		List<Map<String, Integer>> list =  orderDao.selectOrderOfUserGroupByState(
				types.stream().map(MoneyTypeEnum::getType).collect(Collectors.toList()), currentUserId,
				states.stream().map(OrderStateEnum::getState).collect(Collectors.toList()));

		Map<String, Integer> res = new HashMap<>();
		for (Map<String, Integer> map : list) {
			OrderStateEnum status = OrderStateEnum.getEnumByState(map.get("status"));
			Integer count = map.get("number");
			res.put(status.name(), count);
		}
		return res;
	}

	public PageModel<OrderDTO> list(Long userId, PageQueryParamDTO queryParam) {
		Sort sort = Sort.by(Sort.Direction.DESC, "orderSn");

		Specification<Order> specification = (r, q, cb) -> {
			Predicate p = cb.equal(r.get("userId"), userId);
			if (!StringUtils.isEmpty(queryParam.getStatus())) {
				int state= OrderStateEnum.getStateByComment(queryParam.getStatus());
				if (state >= 0) {
					p = cb.and(p, cb.equal(r.get("status"), state));
				}
			}

			Predicate notDel = cb.or(cb.equal(r.get("isDelete"), 0), cb.isNull(r.get("isDelete")));
			Predicate orderType = r.get("orderType").in(NORMAL_GOODS.getType(), RESALE_GOODS.getType());
			if (queryParam.getMoneyTypes() != null && !queryParam.getMoneyTypes().isEmpty()) {
				orderType =  r.get("orderType").in(queryParam.getMoneyTypes());
			}
			p = cb.and(p, notDel, orderType);
			return p;
		};
		Pageable pageableReq = PageRequest.of(Long.valueOf(queryParam.getPageNum()-1).intValue(),
				Long.valueOf(queryParam.getPageSize()).intValue(), sort);
		Page<Order> orders = orderDao.findAll(specification, pageableReq);

		List<OrderDTO> dtos = orders.getContent().stream()
				.map(o->checkOrderExpired(o))
				.map(o->OrderDTO.builder()
				.orderSn(o.getOrderSn()).id(o.getId()).count(o.getCount())
				.totalAmount(o.getTotalAmount()).statusComment(getCommentByState(o.getStatus()))
				.productPic(o.getProductPic()).unitPrice(o.getUnitPrice()).createTime(o.getCreateTime())
				.subject(o.getSubject()).description(o.getDescription())
				.orderType(o.getOrderType()).build())
				.collect(Collectors.toList());
		return PageModel.instance(orders.getTotalElements(), dtos);
	}

	private BigDecimal getSalePrice(GoodsDTO goods) {
		return goods.getPromotionPrice() != null ? goods.getPromotionPrice() : goods.getPrice();
	}

	private BigDecimal getSalePrice(GoodsAttributes attributes) {
		return attributes.getPromotionPrice() != null ? attributes.getPromotionPrice() : attributes.getPrice();
	}

	private UserReceiveAddress getUserReceiveAddress(Long userId, Long addressId) {
		UserReceiveAddress address = null;
		if (addressId == null) {
			address = addressService.defaultAddress(userId);
		} else {
			address = userReceiveAddressDao.findByUserIdAndId(userId, addressId);
		}
		return address;
	}

	private List<OrderItem> generateOrderDetailFromCart(Long userId, List<CartItemParam> carts, Order order) {
		List<OrderItem> orderItems = new ArrayList<>();
		BigDecimal totalAmount = BigDecimal.ZERO;
		StringBuilder desBuilder = new StringBuilder(carts.size() * 4);
		for (CartItemParam cart: carts) {
			OrderItem item = new OrderItem();
			orderItems.add(item);
			item.setName(cart.getProductName());
			item.setQuantity(cart.getQuantity());
			item.setPrice(cart.getPrice());
			item.setProductId(cart.getProductId());
			item.setProductAttributeId(cart.getProductAttrId());
			item.setUserId(userId);
			totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
			desBuilder.append(cart.getProductName()).append(":x").append(item.getQuantity()).append("\n");
		}
		order.setUserId(userId);
		order.setCount(orderItems.size());
		order.setTotalAmount(totalAmount);
		order.setDescription(desBuilder.toString());
		order.setSubject(order.getOrderSn() + ", 共有品类" + order.getCount());
		return orderItems;
	}

	private void setReceiverInfoOfOrder(UserReceiveAddress address, Order order) {
		if (address != null) {
			order.setReceiverCity(address.getCity());
			order.setReceiverProvince(address.getProvince());
			order.setReceiverPostCode(address.getPostCode());
			order.setReceiverCounty(address.getDistrict());
			order.setReceiverTown(address.getTown());
			order.setReceiverDetailAddress(address.getDetailAddress());
			order.setReceiverName(address.getUserName());
			order.setReceiverPhone(address.getUserPhone());
		}
	}

	@Override
	public String generateOrderNo() {
		// TODO use redis increase
		LocalDateTime localDateTime = LocalDateTime.now();
		String rand = RandomStringUtils.randomNumeric(4);
		return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + rand;
	}

	@Override
	public void delivery(List<OrderDeliveryParam> deliveryParamList) {
		
	}

	@Override
	public void update(Order order) {
		
	}

	@Override
	public Order applyToDeliverOrder(Long currentUserId, String orderNo) {
		Order order = orderDao.findFirstByOrderSn(orderNo);
		if (order.getStatus() != PAID.getState()) {
			throw BusinessException.build("只有已付款的订单才能发货");
		}
		order.setStatus(WAIT_DELIVER.getState());
		orderDao.save(order);
		return order;
	}

	@Override
	@Transactional
	public void payByMoney(Long userId, Order order) {
		UserMoney userMoney = userMoneyDao.selectUserForUpdate(userId);
		if (userMoney == null) {
			throw BusinessException.build("用户余额不存在,请联系管理员");
		}

		BigDecimal amount = order.getTotalAmount();
		if (userMoney.getAvailableAmount().compareTo(amount) < 0) {
			throw BusinessException.build("可用余额不足");
		}

		userMoney.setAvailableAmount(userMoney.getAvailableAmount().subtract(amount));
		updatePayInfo(order, null, PayMethodEnum.MONEY.name(),
				MoneyTypeEnum.getDescriptionByState(order.getOrderType()),  amount, userMoney.getAvailableAmount(),
				PayStatusEnum.SUCCESS.name(), "");
		userMoneyDao.save(userMoney);
	}

	@Override
	@Transactional
	public void payByScore(Long userId, Order order) {
		UserScore userScore = userScoreDao.findByUserId(userId);
		if (userScore == null) {
			throw BusinessException.build("用户积分不存在,请联系管理员");
		}

		if (MoneyTypeEnum.RESALE_GOODS.getType() == order.getOrderType()) {
			throw BusinessException.build("订单里的商品不能使用积分.");
		}

		List<OrderItem> items = orderItemDao.findAllByOrOrderIdAndUserId(order.getId(), userId);
		for (OrderItem item: items) {
			if ((item.getScoreRedeem() != null && item.getScoreRedeem() == 0)) {
				throw BusinessException.build("订单里的商品不能使用积分.");
			}
		}
		Long amount = BigDecimal.valueOf(order.getTotalAmount().doubleValue() * scorePayRate).longValue();
		if (userScore.getAvailableScore().compareTo(amount) < 0) {
			throw BusinessException.build("可用积分不足");
		}

		userScore.setAvailableScore(userScore.getAvailableScore() - amount);
		updatePayInfo(order, null, PayMethodEnum.SCORE.name(),
				MoneyTypeEnum.getDescriptionByState(order.getOrderType()),  order.getTotalAmount(), order.getTotalAmount(),
				PayStatusEnum.SUCCESS.name(), "");
		userScoreDao.save(userScore);
	}

	@Autowired
	ILotsService lotsService;

	@Override
	public Order submitAuction(Long userId, Long addressId, Long auctionId) {
		UserReceiveAddress address = getUserReceiveAddress(userId, addressId);
		if (address == null) {
			throw BusinessException.build("地址不存在");
		}

		Lots lots = lotsService.findById(auctionId);
		if (lots == null) {
			throw BusinessException.build("拍品不存在");
		}

		GoodsDTO goods = null;
		if(lots.getGoodsId() != null) {
			goods = goodsService.findGoodsWithAttrById(lots.getGoodsId(), null);
		}

		if (goods == null) {
			throw BusinessException.build("拍卖商品不存在");
		}

		return generateAuctionMarginOrder(userId, address, lots, goods);
	}

	private Order generateAuctionMarginOrder(Long userId, UserReceiveAddress address, Lots lots, GoodsDTO goods) {
		Order order = new Order();
		order.setOrderType(AUCTION_MARGIN.getType());
		setReceiverInfoOfOrder(address, order);
		order.setUserId(userId);
		order.setCount(1);
		order.setOrderSn(generateOrderNo());
		order.setStatus(WAIT_PAYING.getState());
		order.setCreateTime(LocalDateTime.now());
		order.setDescription("拍卖保证金付款." + lots.getId());
		order.setSubject("拍卖订单" + order.getOrderSn());

		OrderItem item = new OrderItem();
		item.setProductId(lots.getGoodsId());
		item.setQuantity(1);
		item.setUserId(userId);
		item.setProductPic(goods.getProductPic());
		item.setUnitPrice(goods.getPromotionPrice() != null ?
				goods.getPromotionPrice() : goods.getPrice());
		order.setProductPic(item.getProductPic());
		order.setTotalAmount(lots.getDeposit());
		order.setAuctionMargin(lots.getDeposit());
		order.setAuctionStartPrice(lots.getStartPrice());
		order.setAuctionId(lots.getId());

		order = orderDao.save(order);
		item.setOrderId(order.getId());
		orderItemDao.save(item);
		return order;
	}

	@Override
	@Transactional
	public Order payAuctionMarginByMoney(Long userId, Long auctionId) {
		Lots lots = lotsService.findById(auctionId);
		if (lots == null) {
			throw BusinessException.build("拍品不存在");
		}

		GoodsDTO goods = null;
		if(lots.getGoodsId() != null) {
			goods = goodsService.findGoodsWithAttrById(lots.getGoodsId(), null);
		}

		if (goods == null) {
			throw BusinessException.build("拍卖商品不存在");
		}

		UserMoney userMoney = userMoneyDao.selectUserForUpdate(userId);
		if (userMoney == null) {
			throw BusinessException.build("用户余额不存在,请联系管理员");
		}

		BigDecimal amount = lots.getDeposit();
		if (userMoney.getAvailableAmount().compareTo(amount) < 0) {
			throw BusinessException.build("可用余额不足");
		}

		Order order = generateAuctionMarginOrder(userId, null, lots, goods);
		order.setPayType(PayMethodEnum.MONEY.getCode());

		userMoney.setAvailableAmount(userMoney.getAvailableAmount().subtract(amount));
		userMoney.setUnavailableAmount(userMoney.getUnavailableAmount().add(amount));
		userMoneyDao.save(userMoney);

		updatePayInfo(order, null, PayMethodEnum.MONEY.name(),
				MoneyTypeEnum.getDescriptionByState(order.getOrderType()),  amount, userMoney.getAvailableAmount(),
				PayStatusEnum.SUCCESS.name(), "");
		return order;
	}
}
