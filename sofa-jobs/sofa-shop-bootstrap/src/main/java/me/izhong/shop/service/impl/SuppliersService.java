package me.izhong.shop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.SuppliersDao;
import me.izhong.shop.entity.Suppliers;
import me.izhong.shop.service.ISuppliersService;

@Service
public class SuppliersService implements ISuppliersService {

	@Autowired
	private SuppliersDao suppliersDao;

	@Override
	@Transactional
	public void saveOrUpdate(Suppliers suppliers) {
		suppliersDao.save(suppliers);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		suppliersDao.deleteById(id);
	}

	@Override
	public Suppliers findById(Long id) {
		return suppliersDao.findById(id).orElseThrow(()-> BusinessException.build("找不到供应商" + id));
	}
}
