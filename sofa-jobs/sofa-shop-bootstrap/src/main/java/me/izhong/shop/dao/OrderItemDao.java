package me.izhong.shop.dao;

import me.izhong.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemDao extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrOrderIdAndUserId(Long orderId, Long userId);

    List<OrderItem> findAllByOrderId(Long orderId);
}
