package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopReceiveAddress;

public interface IShopReceiveAddressMngFacade {

    PageModel<ShopReceiveAddress> pageList(PageRequest request, String phone, String name);
}
