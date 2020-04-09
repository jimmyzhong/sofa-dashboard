package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopVipInfo;

public interface IShopVipInfoMngFacade {

    void create(ShopVipInfo shopVipInfo);

    void edit(ShopVipInfo shopVipInfo);

    boolean remove(String ids);

	PageModel<ShopVipInfo> pageList(PageRequest fromRequest);

	ShopVipInfo find(Long id);
}
