package me.izhong.shop.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.consts.PayStatusEnum;
import me.izhong.shop.dao.OrderDao;
import me.izhong.shop.dao.OrderItemDao;
import me.izhong.shop.dao.PayRecordDao;
import me.izhong.shop.dao.UserReceiveAddressDao;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

		if (Integer.valueOf(WAIT_PAYING.getState()).equals(order.getStatus()) &&
				LocalDateTime.now().isAfter(order.getCreateTime().plusMinutes(30))) {
			order.setStatus(EXPIRED.getState());
			order = saveOrUpdate(order);
		}

		OrderFullDTO dto = new OrderFullDTO();
		BeanUtils.copyProperties(order, dto);
		dto.setItems(items);
		return dto;
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
	public void updatePayInfo(Order order, String externalOrderNo, String payMethod,
							  String payType, BigDecimal payAmount, BigDecimal totalAmount,
							  String state, String comment) {
		PayRecord record = payRecordDao.findFirstByInternalId(order.getOrderSn());
		if (record == null) {
			record = new PayRecord();
		}
		record.setInternalId(order.getOrderSn());
		record.setExternalId(externalOrderNo);
		record.setPayAmount(payAmount);
		record.setTotalAmount(totalAmount);
		record.setPayMethod(payMethod);
		record.setPayForType(payType);
		record.setState(state);

		if (comment.length() > 200) {
			comment = comment.substring(0,200);
		}
		record.setComment(comment);

		order.setPayTradeNo(externalOrderNo);
		order.setPayAmount(payAmount);
		if (PayStatusEnum.SUCCESS.name().equals(state)) {
			order.setStatus(PAID.getState());
		}

		payRecordDao.save(record);
		orderDao.save(order);
		//TODO 扣减库存
	}

	/**
	 * 直接买
	 * 1.创建订单和订单明细
	 * 2.清空购物车
	 * 3.减库存
	 */
	@Override
	@Transactional
	public Order submit(Long userId, Long addressId, Long productId, Long productAttrId, Integer quantity) {
		UserReceiveAddress address = getUserReceiveAddress(userId, addressId);
		if (address == null) {
			throw BusinessException.build("地址不存在");
		}

		//TODO 预减库存
		GoodsDTO goods = goodsService.findGoodsWithAttrById(productId, productAttrId);
		if (goods == null) {
			throw BusinessException.build("商品不存在");
		}

		Order order = new Order();
		setReceiverInfoOfOrder(address, order);

		OrderItem item = new OrderItem();
		item.setProductId(goods.getId());
		item.setQuantity(quantity); // TODO
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
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("userId", ExampleMatcher.GenericPropertyMatchers.exact());

		Example<Order> example = Example.of(order, matcher);
		Sort sort = Sort.by(Sort.Direction.DESC, "orderSn");

		Pageable pageableReq = PageRequest.of(Long.valueOf(queryParam.getPageNum()-1).intValue(),
				Long.valueOf(queryParam.getPageSize()).intValue(), sort);
		Page<Order> orders = orderDao.findAll(example, pageableReq);
		List<OrderDTO> dtos = orders.getContent().stream().map(o->OrderDTO.builder()
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
}
