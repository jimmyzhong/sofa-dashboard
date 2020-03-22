package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopVersions;

public interface IShopVersionsMngFacade {

    void create(ShopVersions shopVersions);

    void edit(ShopVersions shopVersions);

	PageModel<ShopVersions> pageList(PageRequest fromRequest, String type);

	ShopVersions find(Long id);
}
