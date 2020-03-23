package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopAppVersions;

public interface IShopAppVersionsMngFacade {

    void create(ShopAppVersions shopAppVersions);

    void edit(ShopAppVersions shopAppVersions);

    boolean remove(String ids);

	PageModel<ShopAppVersions> pageList(PageRequest fromRequest, String type);

	ShopAppVersions find(Long id);
}
