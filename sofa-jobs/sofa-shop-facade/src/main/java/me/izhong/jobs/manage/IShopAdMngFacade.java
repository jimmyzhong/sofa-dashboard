package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopAd;

public interface IShopAdMngFacade {

    void create(ShopAd shopAd);

    void edit(ShopAd shopAd);

	void updateShowStatus(List<Long> ids, Integer showStatus);

    boolean remove(String ids);

	PageModel<ShopAd> pageList(PageRequest fromRequest, String name, String content);

	ShopAd find(Long adId);
}
