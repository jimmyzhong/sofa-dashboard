package me.izhong.shop.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public GoodsCategory findByChildrenId(Long categoryId) {
		return goodsCategoryDao.findByChildrenId(categoryId);
	}

	@Override
	public List<GoodsCategory> findByParentId(Long parentId) {
		return goodsCategoryDao.findByParentId(parentId);
	}

	@Override
	public List<GoodsCategory> findByLevel1() {
		return goodsCategoryDao.findByLevelAndShowStatus(0, 1);
	}

	@Override
	@Transactional
	public void updateShowStatusByIds(List<Long> ids, Integer showStatus) {
		goodsCategoryDao.updateShowStatus(ids, showStatus);
	}

	@Override
	public void deleteById(Long categoryId) {
		goodsCategoryDao.deleteById(categoryId);
	}
}
