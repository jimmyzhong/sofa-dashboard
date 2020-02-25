package me.izhong.shop.service;

import me.izhong.shop.entity.GoodsCategory;

public interface IGoodsCategoryService {

	GoodsCategory saveOrUpdate(GoodsCategory goodsCategory);

	GoodsCategory findById(Long categoryId);

	void deleteById(Long categoryId);
}
