package me.izhong.shop.service;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Goods;

public interface IGoodsService {

	Goods saveOrUpdate(Goods goods);

	Goods findById(Long goodsId);

	void updatePublishStatusByIds(List<Long> ids, Integer publishStatus);

	void updateRecommendStatusByIds(List<Long> ids, Integer recommendStatus);

	void updateIsDeleteByIds(List<Long> ids, Integer deleteStatus);
	
	void deleteById(Long goodsId);

	void checkGoodsName(Goods goods, String goodsName);

	PageModel<GoodsDTO> list(PageQueryParamDTO queryParam);

	GoodsDTO findGoodsWithAttrById(Long goodsId);
}
