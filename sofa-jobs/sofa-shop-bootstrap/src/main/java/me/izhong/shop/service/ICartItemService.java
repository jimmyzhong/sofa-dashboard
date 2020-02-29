package me.izhong.shop.service;

import java.util.List;

import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.entity.CartItem;

public interface ICartItemService {

	void add(CartItemParam cartItemParam);

	List<CartItemParam> list(Long userId);

	void updateQuantity(Long userId, Long id, Integer quantity);

	void delete(Long userId, List<Long> ids);

	void clear(Long userId);

}
