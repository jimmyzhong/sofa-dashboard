package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopLots;
import me.izhong.jobs.model.ShopUser;

public interface IShopLotsMngFacade {

    void create(ShopLots shopLots);

    void edit(ShopLots shopLots);

    boolean remove(String ids);

	PageModel<ShopLots> pageList(PageRequest request, ShopLots search);

	List<ShopUser> auctionUserPageList(PageRequest request, Long auctionId);

	ShopLots find(Long id);
}
