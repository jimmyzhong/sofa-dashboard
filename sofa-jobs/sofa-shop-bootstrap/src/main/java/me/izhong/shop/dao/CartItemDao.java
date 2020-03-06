package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.CartItem;

import java.util.List;
import java.util.Set;

@Repository
public interface CartItemDao extends JpaRepository<CartItem, Long> {

    CartItem findFirstByUserIdAndProductAttributeIdAndProductId(Long userId, Long productAttrId, Long productId);

    List<CartItem> findCartItemsByUserId(Long userId);

    @Modifying
    @Query(value = "update cart_item ct set ct.quantity = ?3 where ct.id = ?2 and ct.user_id = ?1", nativeQuery = true)
    void updateQuantity(Long userId, Long id, Integer quantity);

    void deleteCartItemsByUserId(Long userId);

    void deleteCartItemsByUserIdAndIdIn(Long userId, Set<Long> ids);
}
