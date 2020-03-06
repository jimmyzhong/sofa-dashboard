package me.izhong.shop.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import me.izhong.shop.dao.CartItemDao;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.dto.CartItemParam;
import me.izhong.shop.entity.CartItem;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.service.ICartItemService;

@Service
public class CartItemService implements ICartItemService {

	@Autowired
	private CartItemDao cartItemDao;
	@Autowired
	private GoodsDao goodsDao;

	@Override
	@Transactional
	public void add(CartItemParam cartItemParam) {
		CartItem cartItem = new CartItem();
		BeanUtils.copyProperties(cartItemParam, cartItem);
		cartItem.setUpdateTime(LocalDateTime.now());
		cartItemDao.save(cartItem);
	}

	@Override
	public List<CartItemParam> list(Long userId) {
		List<CartItem> items = cartItemDao.findCartItemsByUserId(userId);
		if (items != null) {
			return convertDTO(items);
		}
		return Lists.newArrayList();
	}

	private List<CartItemParam> convertDTO(List<CartItem> items) {
		List<CartItemParam> cartItemList = Lists.newArrayList();
		items.stream().forEach(item -> {
			Optional<Goods> optionalGoods = goodsDao.findById(item.getProductId());
			if (optionalGoods.isPresent()) {
				Goods goods = optionalGoods.get();
				CartItemParam param = new CartItemParam();
				BeanUtils.copyProperties(item, param);
				param.setPrice(goods.getPrice());
				param.setProductPic(goods.getProductPic());
				param.setProductName(goods.getProductName());
				param.setProductSn(goods.getProductSn());
				cartItemList.add(param);
			}
		});
		return cartItemList;
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
	public List<CartItemParam> list(List<Long> cartIds) {
		List<CartItem> res = cartItemDao.findAllById(cartIds);
		if (res == null) {
			return new ArrayList<>();
		}
		return convertDTO(res);
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
	public CartItemParam findFirstByUserIdAndProductAttributeIdAndProductId(Long userId, Long productId, Long productAttrId) {
		CartItem cartItem = cartItemDao.findFirstByUserIdAndProductAttributeIdAndProductId(userId, productAttrId, productId);
		if (cartItem != null) {
			CartItemParam param = new CartItemParam();
			BeanUtils.copyProperties(cartItem, param);
			return param;
		}
		return null;
	}

}
