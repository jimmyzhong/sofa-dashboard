package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopLots;

public interface IShopLotsMngFacade {

    void create(ShopLots shopLots);

    void edit(ShopLots shopLots);

    boolean remove(String ids);

	PageModel<ShopLots> pageList(PageRequest request, ShopLots search);

	ShopLots find(Long id);
}
