package me.izhong.shop.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.shop.dao.AdDao;
import me.izhong.shop.entity.Ad;
import me.izhong.shop.service.IAdService;

@Service
public class AdService implements IAdService {

	@Autowired
	private AdDao adDao;

	@Override
	@Transactional
	public void saveOrUpdate(Ad ad) {
		adDao.save(ad);
	}

	@Override
	@Transactional
	public void deleteById(Long adId) {
		adDao.deleteById(adId);
	}

	@Override
	@Transactional
	public void updateStatus(List<Long> ids, Integer status) {
		adDao.updateStatusByIds(ids, status);
	}

	@Override
	public Ad findById(Long adId) {
		return adDao.findById(adId).orElseThrow(()->new RuntimeException("unable to find Ad by " + adId));
	}

}
