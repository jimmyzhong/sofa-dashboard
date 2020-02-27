package me.izhong.shop.service.impl;

import java.util.List;

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
}
