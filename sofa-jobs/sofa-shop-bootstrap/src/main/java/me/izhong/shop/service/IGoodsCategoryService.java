package me.izhong.shop.service;

import java.util.List;

import me.izhong.shop.entity.GoodsCategory;

public interface IGoodsCategoryService {

	GoodsCategory saveOrUpdate(GoodsCategory goodsCategory);

	GoodsCategory findById(Long categoryId);

	void updateShowStatusByIds(Integer publishStatus, List<Long> ids);

	void deleteById(Long categoryId);
}
