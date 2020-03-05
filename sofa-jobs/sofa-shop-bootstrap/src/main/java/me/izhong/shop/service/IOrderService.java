package me.izhong.shop.service;

import java.math.BigDecimal;
import java.util.List;

import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.entity.Order;

public interface IOrderService {

    Order saveOrUpdate(Order order);

    Order findById(Long orderId);

	void deleteById(Long orderId);

	void updateOrderStatusByIds(Integer orderStatus, List<Long> ids);

	void updateReceiverInfoById(ShopReceiverInfo shopReceiverInfo);

	void updateNoteById(Long id, String note);

	Order findByOrderNo(String orderNo);

	void updatePayInfo(Order order, String externalOrderNo, String payMethod, String payType,
					   BigDecimal payAmount, BigDecimal totalAmount, String state, String comment);

	Object submit(Long userId, String body);
}
