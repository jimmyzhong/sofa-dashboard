package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.LotsDao;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.service.ILotsService;

@Service
public class LotsService implements ILotsService {

	@Autowired
	private LotsDao lotsDao;

	@Override
	@Transactional
	public void saveOrUpdate(Lots lots) {
		lotsDao.save(lots);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		lotsDao.deleteById(id);
	}

	@Override
	public Lots findById(Long id) {
		return lotsDao.findById(id).orElseThrow(()-> BusinessException.build("找不到拍卖品" + id));
	}

}
