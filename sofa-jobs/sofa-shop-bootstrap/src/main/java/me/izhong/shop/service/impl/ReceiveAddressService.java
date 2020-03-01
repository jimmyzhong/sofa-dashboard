package me.izhong.shop.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

import me.izhong.shop.dao.UserReceiveAddressDao;
import me.izhong.shop.dto.ReceiveAddressParam;
import me.izhong.shop.entity.UserReceiveAddress;
import me.izhong.shop.service.IReceiveAddressService;

@Service
public class ReceiveAddressService implements IReceiveAddressService {

	@Autowired
	private UserReceiveAddressDao userReceiveAddressDao;

	@Override
	@Transactional
	public void add(ReceiveAddressParam param) {
		UserReceiveAddress address = new UserReceiveAddress();
		BeanUtils.copyProperties(param, address);
		List<UserReceiveAddress> addressList = userReceiveAddressDao.findByUserId(address.getUserId());
		address.setIsDefault(CollectionUtils.isEmpty(addressList) ? 1 : 0);
		userReceiveAddressDao.save(address);
	}

	@Override
	@Transactional
	public void delete(Long userId, Long id) {
		userReceiveAddressDao.deleteByUserIdAndId(userId, id);
	}

	@Override
	public void update(Long userId, ReceiveAddressParam param) {
		UserReceiveAddress address = new UserReceiveAddress();
		BeanUtils.copyProperties(param, address);
		address.setUserId(userId);
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
	
}
