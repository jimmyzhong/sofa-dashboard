package me.izhong.shop.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.izhong.shop.dao.CartItemDao;
import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.entity.CartItem;
import me.izhong.shop.service.ICartItemService;

@Service
public class CartItemService implements ICartItemService {

	@Autowired
	private CartItemDao cartItemDao;

	@Override
	public void add(CartItemParam cartItemParam) {
		CartItem cartItem = new CartItem();
		BeanUtils.copyProperties(cartItemParam, cartItem);
		cartItemDao.save(cartItem);
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
