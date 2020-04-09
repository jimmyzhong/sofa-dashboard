package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.PlatformParamConfigDao;
import me.izhong.shop.entity.PlatformParamConfig;
import me.izhong.shop.service.IPlatformParamConfigService;

@Service
public class PlatformParamConfigService implements IPlatformParamConfigService {

	@Autowired
	private PlatformParamConfigDao platformParamConfigDao;

	@Override
	@Transactional
	public void saveOrUpdate(PlatformParamConfig platformParamConfig) {
		platformParamConfigDao.save(platformParamConfig);
	}

	@Override
	public PlatformParamConfig findById(Long id) {
		return platformParamConfigDao.findById(id).orElseThrow(()-> BusinessException.build("找不到参数配置" + id));
	}
}
