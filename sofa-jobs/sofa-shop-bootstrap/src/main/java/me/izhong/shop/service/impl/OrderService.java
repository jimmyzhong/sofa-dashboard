package me.izhong.shop.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import me.izhong.shop.dao.PayRecordDao;
import me.izhong.shop.entity.PayRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.dao.OrderDao;
import me.izhong.shop.entity.Order;
import me.izhong.shop.service.IOrderService;

@Slf4j
@Service
public class OrderService implements IOrderService {

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private PayRecordDao payRecordDao;

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
	}
}
