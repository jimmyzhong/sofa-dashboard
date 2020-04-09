package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopLotParamInfo;

public interface IShopLotParamInfoMngFacade {

    void create(ShopLotParamInfo shopLotParamInfo);

    void edit(ShopLotParamInfo shopLotParamInfo);

	PageModel<ShopLotParamInfo> pageList(PageRequest fromRequest);

	ShopLotParamInfo find(Long id);
}
