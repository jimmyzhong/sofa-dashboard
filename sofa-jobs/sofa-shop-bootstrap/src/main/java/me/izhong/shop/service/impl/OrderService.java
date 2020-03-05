package me.izhong.shop.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import me.izhong.shop.dao.OrderItemDao;
import me.izhong.shop.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.dao.OrderDao;
import me.izhong.shop.dao.PayRecordDao;
import me.izhong.shop.dao.UserReceiveAddressDao;
import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.dto.ReceiveAddressParam;
import me.izhong.shop.entity.Order;
import me.izhong.shop.entity.PayRecord;
import me.izhong.shop.entity.UserReceiveAddress;
import me.izhong.shop.service.IOrderService;

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
	private CartItemService cartItemService;

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

		payRecordDao.save(record);
		orderDao.save(order);
		//TODO 扣减库存
	}

	/**
	 * 1.创建订单和订单明细
	 * 2.清空购物车
	 * 3.减库存
	 */
	@Override
	@Transactional
	public Object submit(Long userId, String body) {
		JSONObject json = JSONObject.parseObject(body);
		Long cartId = json.getLong("cartId");
        Long addressId = json.getLong("addressId");
        UserReceiveAddress address = userReceiveAddressDao.findByUserIdAndId(userId, addressId);
        if (address == null) {
        	throw BusinessException.build("地址不存在");
        }
        BigDecimal price = new BigDecimal(0);
        CartItemParam cartItemParam = cartItemService.findByCartId(cartId);
        if (cartItemParam == null) {
        	throw BusinessException.build("购物车为空");
        }
        return null;
	}

	@Override
	@Transactional
	public Order submit(Long userId, Long addressId, List<Long> cartIds) {
		UserReceiveAddress address = userReceiveAddressDao.findByUserIdAndId(userId, addressId);
		if (address == null) {
			throw BusinessException.build("地址不存在");
		}
		List<CartItemParam> carts = cartItemService.list(cartIds);
		if (carts.isEmpty()) {
			throw BusinessException.build("找不到订单内容");
		}

		// TODO 校验库存，能否购买，预减库存
		Order order = new Order();
		List<OrderItem> orderItems = new ArrayList<>();
		BigDecimal totalAmount = BigDecimal.ZERO;
		StringBuilder desBuilder = new StringBuilder(carts.size() * 4);
		for (CartItemParam cart: carts) {
			OrderItem item = new OrderItem();
			orderItems.add(item);
			item.setQuantity(cart.getQuantity());
			item.setPrice(cart.getPrice());
			item.setProductId(cart.getProductId());
			item.setProductAttributeId(cart.getProductAttrId());
			item.setUserId(userId);
			totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
			desBuilder.append(cart.getProductName()).append(":x").append(item.getQuantity()).append("\n");
		}
		order.setTotalAmount(totalAmount);
		order.setOrderSn(generateOrderNo());
		order.setSubject(userId + order.getOrderSn());
		order.setDescription(desBuilder.toString());

		order = orderDao.save(order);
		Long orderId = order.getId();
		orderItems.forEach(o->o.setOrderId(orderId));
		orderItemDao.saveAll(orderItems);

		cartItemService.delete(userId,
				carts.stream().map(CartItemParam::getId).collect(Collectors.toList()));
		return order;
	}

	private String generateOrderNo() {
		// TODO use redis increase
		LocalDateTime localDateTime = LocalDateTime.now();
		return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
	}
}
