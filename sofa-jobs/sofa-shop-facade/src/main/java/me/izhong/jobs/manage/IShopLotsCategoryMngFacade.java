package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopLotsCategory;

public interface IShopLotsCategoryMngFacade {

    void create(ShopLotsCategory shopLotsCategory);

    void edit(ShopLotsCategory shopLotsCategory);

    boolean remove(String ids);

	PageModel<ShopLotsCategory> pageList(PageRequest fromRequest, String name);

	ShopLotsCategory find(Integer id);
}
