package me.izhong.shop.service;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.shop.dto.GoodsCategoryDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.GoodsCategory;

public interface IGoodsCategoryService {

	GoodsCategory saveOrUpdate(GoodsCategory goodsCategory);

	GoodsCategory saveOrUpdateWithPathInfo(GoodsCategory goodsCategory);

	GoodsCategory findById(Long categoryId);

	GoodsCategory findByChildrenId(Long categoryId);

	List<GoodsCategory> findByParentId(Long parentId);

	List<GoodsCategory> findByLevel1();

	void updateShowStatusByIds(List<Long> ids, Integer showStatus);

	void deleteById(Long categoryId);

	PageModel<GoodsCategoryDTO> list(PageQueryParamDTO pageQuery);
}
