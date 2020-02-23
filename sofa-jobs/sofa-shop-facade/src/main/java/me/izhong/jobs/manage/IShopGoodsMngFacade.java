package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopGoods;

public interface IShopGoodsMngFacade {

	ShopGoods find(Long goodsId);

    boolean disable(Long goodsId);

    boolean enable(Long goodsId);

    ShopGoods edit(ShopGoods goods);

    PageModel<ShopGoods> pageList(PageRequest request, ShopGoods group);

    boolean remove(Long jobId);
}
