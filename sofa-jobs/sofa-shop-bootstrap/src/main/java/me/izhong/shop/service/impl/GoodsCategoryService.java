package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.GoodsCategoryDao;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IGoodsCategoryService;

@Service
@Slf4j
public class GoodsCategoryService implements IGoodsCategoryService {
	
	@Autowired
	private GoodsCategoryDao goodsCategoryDao;

	@Override
	public GoodsCategory saveOrUpdate(GoodsCategory goodsCategory) {
		return null;
	}

	@Override
	public GoodsCategory findById(Long categoryId) {
		return goodsCategoryDao.findById(categoryId).get();
	}

}
