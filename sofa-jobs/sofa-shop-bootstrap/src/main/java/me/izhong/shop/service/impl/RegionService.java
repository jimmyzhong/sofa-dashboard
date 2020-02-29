package me.izhong.shop.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.CityDao;
import me.izhong.shop.dao.CountyDao;
import me.izhong.shop.dao.ProvinceDao;
import me.izhong.shop.dao.TownDao;
import me.izhong.shop.entity.City;
import me.izhong.shop.entity.County;
import me.izhong.shop.entity.Province;
import me.izhong.shop.entity.Town;
import me.izhong.shop.service.IRegionService;

@Slf4j
@Service
public class RegionService implements IRegionService {

	@Autowired
	private ProvinceDao provinceDao;
	@Autowired
	private CityDao cityDao;
	@Autowired
	private CountyDao countyDao;
	@Autowired
	private TownDao townDao;

	@Override
	public List<Province> getProvinceList() {
		return provinceDao.findAll();
	}

	@Override
	public List<City> getCityListByProvinceCode(String provinceCode) {
		return cityDao.findByProvinceCode(provinceCode);
	}

	@Override
	public List<County> getCountyListByCityCode(String cityCode) {
		return countyDao.findByCityCode(cityCode);
	}

	@Override
	public List<Town> getTownListByCountyCode(String countyCode) {
		return townDao.findByCountyCode(countyCode);
	}

}
