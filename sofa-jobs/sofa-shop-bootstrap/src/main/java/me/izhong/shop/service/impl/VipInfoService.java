package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.VipInfoDao;
import me.izhong.shop.entity.VipInfo;
import me.izhong.shop.service.IVipInfoService;

@Service
public class VipInfoService implements IVipInfoService {

	@Autowired
	private VipInfoDao vipInfoDao;

	@Override
	@Transactional
	public void saveOrUpdate(VipInfo vipInfo) {
		vipInfoDao.save(vipInfo);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		vipInfoDao.deleteById(id);
	}

	@Override
	public VipInfo findById(Long id) {
		return vipInfoDao.findById(id).orElseThrow(()-> BusinessException.build("找不到等级信息" + id));
	}
}
