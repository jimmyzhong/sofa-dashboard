package me.izhong.shop.service;

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


}
