package me.izhong.shop.service;

import me.izhong.shop.entity.Goods;

public interface IGoodsService {

	Goods saveOrUpdate(Goods goods);

	Goods findById(Long goodsId);
	
	void deleteById(Long goodsId);

	void checkGoodsName(Goods goods, String goodsName);

}
