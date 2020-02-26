package me.izhong.shop.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.izhong.shop.entity.CartItem;
import me.izhong.shop.service.ICartItemService;

@Service
public class CartItemService implements ICartItemService {

	@Override
	public void add(CartItem cartItem) {
	}

	@Override
	public void list(Long userId) {
	}

	@Override
	public void updateQuantity(Long userId, Long id, Integer quantity) {
	}

	@Override
	public void delete(Long userId, List<Long> ids) {
	}

	@Override
	public void clear(Long userId) {
	}
	
}
