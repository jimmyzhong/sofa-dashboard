package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopArticles;

public interface IShopArticlesMngFacade {

    void create(ShopArticles articles);

    void edit(ShopArticles articles);

    boolean remove(String ids);

	PageModel<ShopArticles> pageList(PageRequest fromRequest, String title);

	ShopArticles find(Long id);
}
