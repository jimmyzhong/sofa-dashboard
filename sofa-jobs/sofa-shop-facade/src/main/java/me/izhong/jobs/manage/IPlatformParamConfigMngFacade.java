package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopPlatformParamConfig;

public interface IPlatformParamConfigMngFacade {

    void create(ShopPlatformParamConfig shopPlatformParamConfig);

    void edit(ShopPlatformParamConfig shopPlatformParamConfig);

	PageModel<ShopPlatformParamConfig> pageList(PageRequest fromRequest);

	ShopPlatformParamConfig find(Long id);
}
