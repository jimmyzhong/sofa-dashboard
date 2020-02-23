package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.service.IGoodsService;

@Service
@Slf4j
public class GoodsService implements IGoodsService {
	
	@Autowired
	private GoodsDao goodsDao;

	@Override
	public Goods saveOrUpdate(Goods goods) {
		return null;
	}

	@Override
	public Goods findById(Long goodsId) {
		return goodsDao.findById(goodsId).get();
	}

}
