package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.entity.Order;

@Repository
public interface OrderDao extends JpaRepository<Order, Long> {
	
	@Modifying
	@Query(value = "update order t set t.status = ?1 where t.id in ?2", nativeQuery = true)
	void updateOrderStatus(Integer orderStatus, List<Long> ids);

	@Modifying
	@Query(value = "update order t set t.status = ?1 where t.id in ?2", nativeQuery = true)
	void updateReceiverInfo(ShopReceiverInfo shopReceiverInfo);

	@Modifying
	@Query(value = "update order t set t.note = ?1 where t.id = ?2", nativeQuery = true)
	void updateNote(String note, Long id);
}
