package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.dto.CategoryDTO;
import me.izhong.jobs.model.ShopGoodsCategory;

public interface IShopGoodsCategoryMngFacade {

	ShopGoodsCategory find(Long id);

    void edit(ShopGoodsCategory goodsCategory);

    void updateShowStatus(List<Long> ids, Integer publishStatus);

    PageModel<ShopGoodsCategory> pageList(PageRequest request, Long type);

	List<CategoryDTO> queryLevel1();

	List<CategoryDTO> queryAll();

    boolean remove(Long jobId);

    void create(ShopGoodsCategory goodsCategory);
}
