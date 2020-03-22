package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.VersionsDao;
import me.izhong.shop.entity.Versions;
import me.izhong.shop.service.IVersionsService;

@Service
public class VersionsService implements IVersionsService {

	@Autowired
	private VersionsDao versionsDao;

	@Override
	@Transactional
	public void saveOrUpdate(Versions versions) {
		versionsDao.save(versions);
	}

	@Override
	public Versions findById(Long id) {
		return versionsDao.findById(id).orElseThrow(()-> BusinessException.build("找不到版本信息" + id));
	}

}
