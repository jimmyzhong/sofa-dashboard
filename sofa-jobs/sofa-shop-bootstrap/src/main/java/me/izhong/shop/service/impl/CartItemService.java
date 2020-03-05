package me.izhong.shop.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import me.izhong.shop.dao.CartItemDao;
import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.entity.CartItem;
import me.izhong.shop.service.ICartItemService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartItemService implements ICartItemService {

	@Autowired
	private CartItemDao cartItemDao;

	@Override
	@Transactional
	public void add(CartItemParam cartItemParam) {
		CartItem cartItem = cartItemDao.findFirstByUserIdAndProductAttributeIdAndProductId(
				cartItemParam.getUserId(),
				cartItemParam.getProductAttrId(),
				cartItemParam.getProductId()
				);
		if (cartItem == null) {
			cartItem = new CartItem();
			BeanUtils.copyProperties(cartItemParam, cartItem);
		} else {
			cartItem.setQuantity(cartItem.getQuantity() + 1);
		}
		cartItemDao.save(cartItem);
	}

	@Override
	public List<CartItemParam> list(Long userId) {
		List<CartItem> items = cartItemDao.findCartItemsByUserId(userId);
		if (items == null) {
			return new ArrayList<>();
		}
		return convertDTO(items);
	}

	private List<CartItemParam> convertDTO(List<CartItem> items) {
		return items.stream().map(item->{
			CartItemParam param = new CartItemParam();
			BeanUtils.copyProperties(item, param);
			return param;
		}).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void updateQuantity(Long userId, Long id, Integer quantity) {
		cartItemDao.updateQuantity(userId, id, quantity);
	}

	@Override
	@Transactional
	public void delete(Long userId, List<Long> ids) {
		cartItemDao.deleteCartItemsByUserIdAndIdIn(userId, new HashSet<>(ids));
	}

	@Override
	@Transactional
	public void clear(Long userId) {
		cartItemDao.deleteCartItemsByUserId(userId);
	}

	@Override
	public CartItemParam findByCartId(Long cartId) {
		CartItem cartItem = cartItemDao.findById(cartId).orElseThrow(()->new RuntimeException("unable to find cart by " + cartId));
		// TODO update latest product price
		CartItemParam param = new CartItemParam();
		BeanUtils.copyProperties(cartItem, param);
		return param;
	}

	@Override
	public List<CartItemParam> list(List<Long> cartIds) {
		List<CartItem> res = cartItemDao.findAllById(cartIds);
		if (res == null) {
			return new ArrayList<>();
		}
		return convertDTO(res);
	}

}
