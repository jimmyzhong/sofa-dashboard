package me.izhong.shop.service;

import java.util.List;

import me.izhong.shop.dto.GoodsCollectionParam;

public interface ICollectionService {

	void add(GoodsCollectionParam param);

	void delete(Long userId, Long productId);

	List<GoodsCollectionParam> list(Long userId);

}
