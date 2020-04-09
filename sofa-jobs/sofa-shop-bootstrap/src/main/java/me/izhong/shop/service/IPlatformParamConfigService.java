package me.izhong.shop.service;

import me.izhong.shop.entity.PlatformParamConfig;

public interface IPlatformParamConfigService {

    void saveOrUpdate(PlatformParamConfig platformParamConfig);

    PlatformParamConfig findById(Long id);
}
