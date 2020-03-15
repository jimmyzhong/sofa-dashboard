package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopNotice;

public interface IShopNoticeMngFacade {

    void create(ShopNotice notice);

    void edit(ShopNotice notice);

    boolean remove(String ids);

	PageModel<ShopNotice> pageList(PageRequest fromRequest, String title, Integer status);

	ShopNotice find(Long id);
}
