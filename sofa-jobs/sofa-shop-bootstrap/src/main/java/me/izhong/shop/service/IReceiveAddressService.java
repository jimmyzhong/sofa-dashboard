package me.izhong.shop.service;

import java.util.List;

import me.izhong.shop.dto.ReceiveAddressParam;
import me.izhong.shop.entity.UserReceiveAddress;

public interface IReceiveAddressService {

	void add(ReceiveAddressParam param);

	void delete(Long userId, Long id);

	void update(Long userId, ReceiveAddressParam param);

	List<ReceiveAddressParam> list(Long userId);

	ReceiveAddressParam detail(Long userId, Long id);

	void setDefault(Long userId, Long id);

	UserReceiveAddress defaultAddress(Long userId);
}
