package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.dto.CategoryDTO;
import me.izhong.jobs.model.ShopGoodsCategory;

public interface IShopGoodsCategoryMngFacade {

	ShopGoodsCategory findById(Long id);

    void create(ShopGoodsCategory goodsCategory);

    void edit(ShopGoodsCategory goodsCategory);

    void updateShowStatus(List<Long> ids, Integer showStatus);

    boolean remove(String ids);

    PageModel<ShopGoodsCategory> pageList(PageRequest request, Long parentId);

	List<CategoryDTO> queryLevel1();

	List<CategoryDTO> queryAll();
}
