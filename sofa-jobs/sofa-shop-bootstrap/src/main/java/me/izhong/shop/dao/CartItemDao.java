package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.CartItem;

@Repository
public interface CartItemDao extends JpaRepository<CartItem, Long> {
}
