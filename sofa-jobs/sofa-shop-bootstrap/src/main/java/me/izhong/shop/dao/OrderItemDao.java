package me.izhong.shop.dao;

import me.izhong.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemDao extends JpaRepository<OrderItem, Long> {
}
