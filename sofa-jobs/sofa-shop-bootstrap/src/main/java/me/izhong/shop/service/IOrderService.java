package me.izhong.shop.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import me.izhong.common.domain.PageModel;
import me.izhong.jobs.dto.OrderDeliveryParam;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.dto.order.OrderDTO;
import me.izhong.shop.dto.order.OrderFullDTO;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.Order;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.impl.AliPayService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface IOrderService {

    @Transactional
    void updateExpiredOrders();

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

    @Transactional
    void delete(Long currentUserId, String orderNo);

    Map<String, Integer> getCountOfStatus(Long currentUserId, List<MoneyTypeEnum> type, List<OrderStateEnum> states);

    PageModel<OrderDTO> list(Long userId, PageQueryParamDTO queryParam);

	boolean transferMoney(User user, String orderNo, Order order, AliPayService aliPayService);

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

    String generateOrderNo();

    void delivery(List<OrderDeliveryParam> deliveryParamList);

	void update(Order order);

	/**
	 * 申请发货
	 * @param currentUserId
	 * @param orderNo
	 * @return
	 */
    Order applyToDeliverOrder(Long currentUserId, String orderNo);

	void payByMoney(Long id, Order order);

    void payByScore(Long id, Order order);

	@Deprecated // NOT USED
    Order submitAuction(Long userId, Long addressId, Long auctionId);

	/**
	 * 创建拍卖尾款订单
	 * @param userId
	 * @param auction 拍品
	 * @param finalPrice 拍卖价
	 * @return
	 */
    Order generateAuctionRemainingOrder(Long userId, Lots auction, BigDecimal finalPrice);

	@Transactional(propagation= Propagation.NESTED)
	void refundMargin(Long userId, Lots auction);

	Order payAuctionMarginByMoney(Long id, Long auctionId);

	Order payAuctionRemainByMoney(Long id, Long auctionId);
}
