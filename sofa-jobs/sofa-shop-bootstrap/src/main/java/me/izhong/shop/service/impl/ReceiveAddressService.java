package me.izhong.shop.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.*;
import me.izhong.shop.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

import me.izhong.shop.dto.ReceiveAddressParam;
import me.izhong.shop.service.IReceiveAddressService;

@Service
public class ReceiveAddressService implements IReceiveAddressService {

	@Autowired
	private UserReceiveAddressDao userReceiveAddressDao;
	@Autowired
	private CityDao cityDao;
	@Autowired
	private CountyDao countyDao;
	@Autowired
	private TownDao townDao;
	@Autowired
	private ProvinceDao provinceDao;

	@Override
	@Transactional
	public void add(ReceiveAddressParam param) {
		UserReceiveAddress address = new UserReceiveAddress();
		BeanUtils.copyProperties(param, address);
		updateDefaultAddress(address);

		userReceiveAddressDao.save(address);
	}

	private void updateDefaultAddress(UserReceiveAddress address) {
		UserReceiveAddress defaultAddress = defaultAddress(address.getUserId());
		if (defaultAddress == null) {
			address.setIsDefault(1);
		} else {
			if (address.getIsDefault()!=null && address.getIsDefault()==1
				&& defaultAddress.getId() != address.getId()) {
				address.setIsDefault(1);
				defaultAddress.setIsDefault(0);
				userReceiveAddressDao.save(defaultAddress);
			}
		}
	}

	@Override
	@Transactional
	public void delete(Long userId, Long id) {
		UserReceiveAddress address = defaultAddress(userId);
		// 删除的是默认地址
		if (address != null && address.getId() == id) {
			UserReceiveAddress anotherAddress = userReceiveAddressDao.findByUserId(userId)
					.stream().filter(u->u.getId()!=id).findFirst().orElse(null);
			if (anotherAddress != null) {
				anotherAddress.setIsDefault(1);
				userReceiveAddressDao.save(anotherAddress);
			}
		}
		userReceiveAddressDao.deleteByUserIdAndId(userId, id);
	}

	@Override
	@Transactional
	public void update(Long userId, Long addressId, ReceiveAddressParam param) {
		UserReceiveAddress address = new UserReceiveAddress();
		BeanUtils.copyProperties(param, address);
		address.setUserId(userId);
		address.setId(addressId);
		updateDefaultAddress(address);
		userReceiveAddressDao.save(address);
	}

	@Override
	public List<ReceiveAddressParam> list(Long userId) {
		List<UserReceiveAddress> addressList = userReceiveAddressDao.findByUserId(userId);
		if (CollectionUtils.isEmpty(addressList)) {
			return Lists.newArrayList();
		}
		return addressList.stream().map(t -> {
			ReceiveAddressParam param = new ReceiveAddressParam();
			BeanUtils.copyProperties(t, param);
			return param;
		}).collect(Collectors.toList());
	}

	@Override
	public ReceiveAddressParam detail(Long userId, Long id) {
		UserReceiveAddress address = userReceiveAddressDao.findByUserIdAndId(userId, id);
		ReceiveAddressParam param = new ReceiveAddressParam();
		BeanUtils.copyProperties(address, param);
		return param;
	}

	@Override
	@Transactional
	public void setDefault(Long userId, Long id) {
		UserReceiveAddress addr = userReceiveAddressDao.findByUserIdAndId(userId, id);
		if (addr == null) {
			throw BusinessException.build("收货地址不存在");
		}
		updateDefaultAddress(addr);
		userReceiveAddressDao.save(addr);
	}

	@Override
	public UserReceiveAddress defaultAddress(Long userId) {
		return userReceiveAddressDao.findByUserIdAndIsDefault(userId, 1);
	}

	@Override
	public List<Town> listTown(String countyCode, String prefix) {
		Town town = new Town();
		if (!StringUtils.isEmpty(prefix)) {
			town.setTownName(prefix);
		}
		if (!StringUtils.isEmpty(countyCode)) {
			town.setCountyCode(countyCode);
		}
		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("townName", ExampleMatcher.GenericPropertyMatchers.startsWith())
				.withMatcher("countyCode", ExampleMatcher.GenericPropertyMatchers.exact());
		Example<Town> example = Example.of(town, matcher);
		return townDao.findAll(example);
	}

	@Override
	public List<County> listCounty(String cityCode, String prefix) {
		County county = new County();
		if (!StringUtils.isEmpty(prefix)) {
			county.setCountyName(prefix);
		}
		if (!StringUtils.isEmpty(cityCode)) {
			county.setCityCode(cityCode);
		}
		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("countyName", ExampleMatcher.GenericPropertyMatchers.startsWith())
				.withMatcher("cityCode", ExampleMatcher.GenericPropertyMatchers.exact());
		Example<County> example = Example.of(county, matcher);
		return countyDao.findAll(example);
	}

	@Override
	public List<City> listCity(String provinceCode, String prefix) {
		City city = new City();
		if (!StringUtils.isEmpty(prefix)) {
			city.setCityName(prefix);
		}
		if (!StringUtils.isEmpty(provinceCode)) {
			city.setProvinceCode(provinceCode);
		}
		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("cityName", ExampleMatcher.GenericPropertyMatchers.startsWith())
				.withMatcher("provinceCode", ExampleMatcher.GenericPropertyMatchers.exact());
		Example<City> example = Example.of(city, matcher);
		return cityDao.findAll(example);
	}

	@Override
	public List<Province> listProvince(String prefix) {
		Province province = new Province();
		if (!StringUtils.isEmpty(prefix)) {
			province.setProvinceName(prefix);
		}
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("provinceName", ExampleMatcher.GenericPropertyMatchers.startsWith());
		Example<Province> example = Example.of(province, matcher);
		return provinceDao.findAll(example);
	}
}
