package me.izhong.shop.service;

import java.util.List;

import org.springframework.data.domain.Page;

import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsCategory;

public interface IHomeService {
	
	public Page<Goods> recommendProductList(Integer pageNum, Integer pageSize);

	public List<GoodsCategory> productCategoryList(Long parentId);
}
