package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopLotsItem;

public interface IShopLotsItemMngFacade {

	PageModel<ShopLotsItem> pageList(PageRequest request, Long auctionId);

}
