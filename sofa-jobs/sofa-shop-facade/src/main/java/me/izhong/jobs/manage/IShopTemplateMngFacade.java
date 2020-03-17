package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopTemplate;

public interface IShopTemplateMngFacade {

    void create(ShopTemplate template);

    void edit(ShopTemplate template);

    boolean remove(String ids);

	PageModel<ShopTemplate> pageList(PageRequest fromRequest, String title);

	ShopTemplate find(Long id);
}
