package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopGoods;

public interface IShopGoodsMngFacade {

	ShopGoods find(Long goodsId);

    boolean disable(Long goodsId);

    boolean enable(Long goodsId);

    ShopGoods edit(ShopGoods goods);

    void updatePublishStatus(List<Long> ids, Integer publishStatus);

    void updateRecommendStatus(List<Long> ids, Integer recommendStatus);

    void updateDeleteStatus(List<Long> ids, Integer deleteStatus);

    PageModel<ShopGoods> pageList(PageRequest request, ShopGoods group);

    boolean remove(Long jobId);

    void create(ShopGoods goods);
}
