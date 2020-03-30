package me.izhong.jobs.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.izhong.jobs.admin.service.ShopServiceReference;
import me.izhong.jobs.model.ShopRegion;

@Controller
@RequestMapping("/ext/shop/region")
public class ShopRegionController {

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@GetMapping(value = "/province")
    @ResponseBody
	public Map<String, Object> provinceList() {
		List<ShopRegion> provinceList = shopServiceReference.regionService.getProvinceList();
		Map<String, Object> map = new HashMap<>();
		map.put("provinceList", provinceList);
		return map;
	}

	@GetMapping(value = "/city")
    @ResponseBody
	public Map<String, Object> cityList(@RequestParam(value = "provinceCode", required = true) String provinceCode) {
		List<ShopRegion> provinceList = shopServiceReference.regionService.getCityList(provinceCode);
		Map<String, Object> map = new HashMap<>();
		map.put("cityList", provinceList);
		return map;
	}

	@GetMapping(value = "/county")
    @ResponseBody
	public Map<String, Object> countyList(@RequestParam(value = "cityCode", required = true) String cityCode) {
		List<ShopRegion> provinceList = shopServiceReference.regionService.getCountyList(cityCode);
		Map<String, Object> map = new HashMap<>();
		map.put("countyList", provinceList);
		return map;
	}

	@GetMapping(value = "/town")
    @ResponseBody
	public Map<String, Object> townList(@RequestParam(value = "countyCode", required = true) String countyCode) {
		List<ShopRegion> provinceList = shopServiceReference.regionService.getTownList(countyCode);
		Map<String, Object> map = new HashMap<>();
		map.put("townList", provinceList);
		return map;
	}
}
