package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.GoodsCategoryDao;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IGoodsCategoryService;

@Slf4j
@Service
public class GoodsCategoryService implements IGoodsCategoryService {
	
	@Autowired
	private GoodsCategoryDao goodsCategoryDao;

	@Override
	public GoodsCategory saveOrUpdate(GoodsCategory goodsCategory) {
		return goodsCategoryDao.save(goodsCategory);
	}

	@Override
	public GoodsCategory findById(Long categoryId) {
		return goodsCategoryDao.findById(categoryId).orElseThrow(() -> new RuntimeException("unable to find goodsCategory by " + categoryId));
	}

	@Override
	public void deleteById(Long categoryId) {
		goodsCategoryDao.deleteById(categoryId);
	}

}
