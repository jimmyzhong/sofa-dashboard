package me.izhong.shop.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import me.izhong.shop.dao.GoodsCategoryDao;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IHomeService;

@Service
public class HomeService implements IHomeService {

	@Autowired
	private GoodsDao goodsDao;
	@Autowired
	private GoodsCategoryDao goodsCategoryDao;

	public Page<Goods> recommendProductList(Integer pageNum, Integer pageSize) {
		Goods goods = new Goods();
		goods.setPublishStatus(1);
		goods.setRecommandStatus(1);
		goods.setIsDelete(0);
		Sort sort = new Sort(Sort.Direction.DESC, "createTime");
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        ExampleMatcher matcher = ExampleMatcher.matchingAny().withIgnoreNullValues();
        Example<Goods> example = Example.of(goods, matcher);
		return goodsDao.findAll(example, pageable);
	}

	public List<GoodsCategory> productCategoryList(Long parentId) {
		GoodsCategory goodsCategory = new GoodsCategory();
		goodsCategory.setParentId(parentId);
		goodsCategory.setShowStatus(1);
		Example<GoodsCategory> example = Example.of(goodsCategory);
		return goodsCategoryDao.findAll(example);
	}

}
