package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopGoodsCategory;

public interface IShopGoodsCategoryMngFacade {

	ShopGoodsCategory find(Long id);

    boolean disable(Long id);

    boolean enable(Long id);

    ShopGoodsCategory edit(ShopGoodsCategory goodsCategory);

    PageModel<ShopGoodsCategory> pageList(PageRequest request, ShopGoodsCategory ShopGoodsCategory);

    boolean remove(Long jobId);

    void create(ShopGoodsCategory goodsCategory);
}
