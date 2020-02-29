package me.izhong.shop.service;

import java.util.List;

import me.izhong.shop.entity.City;
import me.izhong.shop.entity.County;
import me.izhong.shop.entity.Province;
import me.izhong.shop.entity.Town;

public interface IRegionService {

	List<Province> getProvinceList();

	List<City> getCityListByProvinceCode(String provinceCode);

	List<County> getCountyListByCityCode(String cityCode);

	List<Town> getTownListByCountyCode(String countyCode);
}
