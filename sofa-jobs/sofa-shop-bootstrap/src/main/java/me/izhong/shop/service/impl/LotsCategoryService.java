package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.LotsCategoryDao;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.service.ILotsCategoryService;

@Service
public class LotsCategoryService implements ILotsCategoryService {

	@Autowired
	private LotsCategoryDao lotsCategoryDao;

	@Override
	@Transactional
	public void saveOrUpdate(LotsCategory lotsCategory) {
		lotsCategoryDao.save(lotsCategory);
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {
		lotsCategoryDao.deleteById(id);
	}

	@Override
	public LotsCategory findById(Integer id) {
		return lotsCategoryDao.findById(id).orElseThrow(()-> BusinessException.build("找不到拍卖分区" + id));
	}
}
