package me.izhong.shop.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.dto.OrderDeliveryParam;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.annotation.NeedOptimisticLockRetry;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.consts.PayStatusEnum;
import me.izhong.shop.dao.*;
import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.dto.order.OrderDTO;
import me.izhong.shop.dto.order.OrderFullDTO;
import me.izhong.shop.entity.*;
import me.izhong.shop.service.IGoodsService;
import me.izhong.shop.service.IOrderService;
import me.izhong.shop.service.IReceiveAddressService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.izhong.shop.consts.OrderStateEnum.*;
import static me.izhong.shop.consts.MoneyTypeEnum.NORMAL_GOODS;

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

	@Value("${order.expire.time}")
	private Long orderExpireMinutes;

	@PostConstruct
	public void setUp() {
		ScheduledExecutorService orderStatusUpdater = Executors.newSingleThreadScheduledExecutor(
				new ThreadFactoryBuilder()
				.setNameFormat("order-status-updater").build());
		orderStatusUpdater.scheduleAtFixedRate(() -> {
			try {
				updateExpiredOrders();
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
					List<OrderItem> items = orderItemDao.findAllByOrOrderIdAndUserId(order.getId(), order.getUserId());
					items.stream().forEach(i->{
						GoodsStore store = storeDao.findByProductIdAndProductAttrId(i.getProductId(),
								i.getProductAttributeId());
						store.setPreStore(store.getPreStore() + i.getQuantity());
						storeDao.save(store);
					});
				}
				orderDao.updateOrderStatus(unpaidOrders.stream().map(Order::getId).collect(Collectors.toList()),
						EXPIRED.getState());
			}
		} finally {
			log.info("task finish time: " + (System.currentTimeMillis() - start));
		}
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
		PayRecord record = payRecordDao.findFirstByInternalIdAndType(order.getOrderSn(), payType);
		if (record == null) {
			record = new PayRecord();
			record.setCreateTime(LocalDateTime.now());
		}
		record.setInternalId(order.getOrderSn());
		record.setExternalId(externalOrderNo);
		record.setPayAmount(payAmount);
		record.setTotalAmount(totalAmount);
		record.setPayMethod(payMethod);
		record.setType(payType);
		record.setPayerId(order.getUserId());
		record.setState(state);

		comment = StringUtils.substring(comment,0,200);
		record.setComment(comment);

		order.setPayTradeNo(externalOrderNo);
		order.setPayAmount(payAmount);
		if (PayStatusEnum.SUCCESS.name().equals(state) && order.getStatus() < PAID.getState()) {
			order.setStatus(PAID.getState());
			List<OrderItem> items = orderItemDao.findAllByOrOrderIdAndUserId(order.getId(),order.getUserId());
			// update stock/sale info
			Set<Goods> goodsSet = new HashSet<>();
			for (OrderItem item: items) {
				GoodsStore store = storeDao.findByProductIdAndProductAttrId(item.getProductId(), item.getProductAttributeId());
				store.setStore(store.getStore() - item.getQuantity());
				storeDao.save(store);
				goodsDao.findById(item.getProductId()).ifPresent(goods->{
					goods.setStock(goods.getStock() - item.getQuantity());
					goods.setSale((goods.getSale()==null ? 0 : goods.getSale()) + item.getQuantity());
					goodsSet.add(goods);
				});
			}
			goodsDao.saveAll(goodsSet);

			// update user money back
			User user = userService.findById(order.getUserId());
			if (user.getInviteUserId() != null) {
				recordMoneyReturn(order, user.getId(), user.getInviteUserId(), 0.03);  //TODO externalize
			}
			if (user.getInviteUserId2() != null) {
				recordMoneyReturn(order, user.getId(), user.getInviteUserId2(), 0.02);  //TODO externalize
			}
		}

		payRecordDao.save(record);
		orderDao.save(order);
	}

	private void recordMoneyReturn(Order order, Long payerId, Long receiverId, Double returnFactor) {
		PayRecord moneyReturn = new PayRecord();
		moneyReturn.setType(MoneyTypeEnum.RETURN_MONEY.getDescription());
		moneyReturn.setCreateTime(LocalDateTime.now());
		moneyReturn.setPayerId(payerId);
		moneyReturn.setReceiverId(receiverId);
		moneyReturn.setTotalAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(returnFactor)));
		moneyReturn.setInternalId(order.getOrderSn());
		payRecordDao.save(moneyReturn);

		UserMoney userMoney = userMoneyDao.findByUserId(receiverId);
		if (userMoney == null) {
			userMoney = new UserMoney();
			userMoney.setUserId(order.getUserId());
			userMoney.setCreateTime(LocalDateTime.now());
			userMoney.setAvailableAmount(moneyReturn.getTotalAmount());
		} else {
			userMoney.setAvailableAmount(userMoney.getAvailableAmount().add(moneyReturn.getTotalAmount()));
			userMoney.setUpdateTime(LocalDateTime.now());
		}
		userMoneyDao.save(userMoney);
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

		// 预减库存
		GoodsStore store = storeDao.findByProductIdAndProductAttrId(productId, productAttrId);
		if (store.getPreStore() < quantity || store.getStore() < quantity) {
			throw BusinessException.build("库存不足");
		}
		store.setPreStore(store.getPreStore() - quantity);
		storeDao.save(store);

		Order order = new Order();
		order.setOrderType(NORMAL_GOODS.getType());
		setReceiverInfoOfOrder(address, order);

		OrderItem item = new OrderItem();
		item.setProductId(goods.getId());
		item.setQuantity(quantity);
		item.setUserId(userId);
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
		order.setStatus(CONFIRMED.getState());
		orderDao.save(order);
		return order;
	}

	@Override
	@Transactional
	public Order cancel(Long currentUserId, String orderNo) {
		Order order = orderDao.findFirstByOrderSn(orderNo);
		order.setStatus(CANCELED.getState());
		// TODO 恢复预扣库存
		orderDao.save(order);
		return order;
	}

	public PageModel<OrderDTO> list(Long userId, PageQueryParamDTO queryParam) {
		Order order = new Order();
		order.setUserId(userId);
		if (!StringUtils.isEmpty(queryParam.getStatus())) {
			int state= OrderStateEnum.getStateByComment(queryParam.getStatus());
			if (state >= 0) {
				order.setStatus(state);
			}
		}
		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("userId", ExampleMatcher.GenericPropertyMatchers.exact())
				.withMatcher("status", ExampleMatcher.GenericPropertyMatchers.exact());

		Example<Order> example = Example.of(order, matcher);
		Sort sort = Sort.by(Sort.Direction.DESC, "orderSn");

		Pageable pageableReq = PageRequest.of(Long.valueOf(queryParam.getPageNum()-1).intValue(),
				Long.valueOf(queryParam.getPageSize()).intValue(), sort);
		Page<Order> orders = orderDao.findAll(example, pageableReq);
		List<OrderDTO> dtos = orders.getContent().stream()
				.map(o->checkOrderExpired(o))
				.map(o->OrderDTO.builder()
				.orderSn(o.getOrderSn()).id(o.getId()).count(o.getCount())
				.totalAmount(o.getTotalAmount()).statusComment(getCommentByState(o.getStatus()))
				.subject(o.getSubject()).description(o.getDescription()).build())
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
		order.setReceiverCity(address.getCity());
		order.setReceiverProvince(address.getProvince());
		order.setReceiverPostCode(address.getPostCode());
		order.setReceiverDetailAddress(address.getDetailAddress());
		order.setReceiverName(address.getUserName());
		order.setReceiverPhone(address.getUserPhone());
	}

	private String generateOrderNo() {
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
}
