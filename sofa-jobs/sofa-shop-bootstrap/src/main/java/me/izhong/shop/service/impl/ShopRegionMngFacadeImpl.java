package me.izhong.shop.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.manage.IShopRegionMngFacade;
import me.izhong.jobs.model.ShopRegion;
import me.izhong.shop.entity.City;
import me.izhong.shop.entity.County;
import me.izhong.shop.entity.Province;
import me.izhong.shop.entity.Town;
import me.izhong.shop.service.IRegionService;

@Slf4j
@Service
@SofaService(interfaceType = IShopRegionMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopRegionMngFacadeImpl implements IShopRegionMngFacade {

	@Autowired
	private IRegionService regionService;

	@Override
	public List<ShopRegion> getProvinceList() {
		List<Province> provinceList = regionService.getProvinceList();
		return provinceList.stream().map(t -> {
			ShopRegion region = new ShopRegion();
			BeanUtils.copyProperties(t, region);
			return region;
		}).collect(Collectors.toList());
	}

	@Override
	public List<ShopRegion> getCityList(String provinceCode) {
		List<City> cityList = regionService.getCityListByProvinceCode(provinceCode);
		return cityList.stream().map(t -> {
			ShopRegion region = new ShopRegion();
			BeanUtils.copyProperties(t, region);
			return region;
		}).collect(Collectors.toList());
	}

	@Override
	public List<ShopRegion> getCountyList(String cityCode) {
		List<County> countyList = regionService.getCountyListByCityCode(cityCode);
		return countyList.stream().map(t -> {
			ShopRegion region = new ShopRegion();
			BeanUtils.copyProperties(t, region);
			return region;
		}).collect(Collectors.toList());
	}

	@Override
	public List<ShopRegion> getTownList(String countyCode) {
		List<Town> townList = regionService.getTownListByCountyCode(countyCode);
		return townList.stream().map(t -> {
			ShopRegion region = new ShopRegion();
			BeanUtils.copyProperties(t, region);
			return region;
		}).collect(Collectors.toList());
	}
}
