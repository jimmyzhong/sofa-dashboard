package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.LotParamInfoDao;
import me.izhong.shop.entity.LotParamInfo;
import me.izhong.shop.service.ILotParamInfoService;

@Service
public class LotParamInfoService implements ILotParamInfoService {

	@Autowired
	private LotParamInfoDao lotParamInfoDao;

	@Override
	@Transactional
	public void saveOrUpdate(LotParamInfo lotParamInfo) {
		lotParamInfoDao.save(lotParamInfo);
	}

	@Override
	public LotParamInfo findById(Long id) {
		return lotParamInfoDao.findById(id).orElseThrow(()-> BusinessException.build("找不到拍卖奖励参数配置" + id));
	}

	@Override
	public LotParamInfo findByType(Long type) {
		return lotParamInfoDao.findByType(type);
	}
}
