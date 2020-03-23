package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.AppVersionsDao;
import me.izhong.shop.entity.AppVersions;
import me.izhong.shop.service.IAppVersionsService;

@Service
public class AppVersionsService implements IAppVersionsService {

	@Autowired
	private AppVersionsDao appVersionsDao;

	@Override
	@Transactional
	public void saveOrUpdate(AppVersions versions) {
		appVersionsDao.save(versions);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		appVersionsDao.deleteById(id);
	}

	@Override
	public AppVersions findById(Long id) {
		return appVersionsDao.findById(id).orElseThrow(()-> BusinessException.build("找不到版本信息" + id));
	}

}
