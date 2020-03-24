package me.izhong.shop.service;

import me.izhong.shop.entity.AppVersions;

public interface IAppVersionsService {

    void saveOrUpdate(AppVersions versions);

	void deleteById(Long id);

    AppVersions findById(Long id);

    AppVersions latest(String type);
}
