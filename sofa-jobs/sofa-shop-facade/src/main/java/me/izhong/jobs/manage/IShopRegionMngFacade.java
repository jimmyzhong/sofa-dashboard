package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.jobs.model.ShopRegion;

public interface IShopRegionMngFacade {

	List<ShopRegion> getProvinceList();

	List<ShopRegion> getCityList(String provinceCode);

	List<ShopRegion> getCountyList(String cityCode);

	List<ShopRegion> getTownList(String countyCode);
}
