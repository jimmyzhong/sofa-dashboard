package me.izhong.shop.service;

import java.util.List;

import me.izhong.shop.entity.Goods;

public interface IGoodsService {

	Goods saveOrUpdate(Goods goods);

	Goods findById(Long goodsId);

	void updatePublishStatusByIds(Integer publishStatus, List<Long> ids);

	void updateRecommendStatusByIds(Integer recommendStatus, List<Long> ids);

	void updateIsDeleteByIds(Integer deleteStatus, List<Long> ids);
	
	void deleteById(Long goodsId);

	void checkGoodsName(Goods goods, String goodsName);

}
