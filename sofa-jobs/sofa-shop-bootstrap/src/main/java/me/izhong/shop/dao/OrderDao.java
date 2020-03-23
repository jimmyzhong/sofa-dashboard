package me.izhong.shop.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.entity.Order;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderDao extends JpaRepository<Order, Long>, JpaSpecificationExecutor {

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
}
