package me.izhong.shop.service;

import java.math.BigDecimal;
import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.dto.order.OrderDTO;
import me.izhong.shop.dto.order.OrderFullDTO;
import me.izhong.shop.entity.Order;

public interface IOrderService {

    Order saveOrUpdate(Order order);

    Order findById(Long orderId);

	OrderFullDTO findFullOrderByOrderNo(String orderNo);

	void deleteById(Long orderId);

	void updateOrderStatusByIds(Integer orderStatus, List<Long> ids);

	void updateReceiverInfoById(ShopReceiverInfo shopReceiverInfo);

	void updateNoteById(Long id, String note);

	Order findByOrderNo(String orderNo);

	void updatePayInfo(Order order, String externalOrderNo, String payMethod, String payType,
					   BigDecimal payAmount, BigDecimal totalAmount, String state, String comment);

	PageModel<OrderDTO> list(Long userId, PageQueryParamDTO queryParam);

	/**
	 * 直接购买某个商品
	 * @param userId
	 * @param productId
	 * @param quantity
	 * @return
	 */
	Order submit(Long userId,  Long addressId, Long productId, Long productAttrId, Integer quantity);

	/**
	 * 购买购物车里的商品
	 * @param userId
	 * @param addressId
	 * @param cartIds
	 * @return
	 */
	Order submit(Long userId, Long addressId, List<Long> cartIds);

	Order confirm(Long userId, String orderNo);

	Order cancel(Long currentUserId, String orderNo);
}
