package me.izhong.shop.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.entity.Order;

@Repository
public interface OrderDao extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

	Order findFirstByOrderSn(String orderSn);

	List<Order> findAllByStatusAndCreateTimeBeforeOrderByOrderSnDesc(Integer status, LocalDateTime time);

	@Modifying
	@Transactional
	@Query(value = "update tx_order t set t.status = ?2 where t.id in ?1", nativeQuery = true)
	void updateOrderStatus(List<Long> ids, Integer orderStatus);

	@Modifying
	@Query(value = "update tx_order t set t.status = ?1 where t.id in ?2", nativeQuery = true)
	void updateReceiverInfo(ShopReceiverInfo shopReceiverInfo);

	@Modifying
	@Query(value = "update tx_order t set t.note = ?1 where t.id = ?2", nativeQuery = true)
	void updateNote(String note, Long id);

	@Query(value = "select status, count(*) as number from shop.tx_order where order_type in ?1 and status in ?3 and user_id = ?2 group by status", nativeQuery = true)
	List<Map<String, Integer>> selectOrderOfUserGroupByState(List<Integer> orderTypes, Long userId, List<Integer> status);

	@Query(value = "select o.* from tx_order o, user u where o.user_id = u.id and o.order_type = ?1 " +
			"and o.AUCTION_ID = ?2 and o.status = ?3 and u.id = ?4", nativeQuery = true)
	Order getOrderOfAuction(Integer type, Long auctionId, Integer status, Long userId);
}
