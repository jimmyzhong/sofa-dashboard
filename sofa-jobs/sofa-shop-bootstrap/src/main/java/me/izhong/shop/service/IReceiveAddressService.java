package me.izhong.shop.service;

import java.util.List;

import me.izhong.shop.dto.ReceiveAddressParam;

public interface IReceiveAddressService {

	void add(ReceiveAddressParam param);

	void delete(Long userId, Long id);

	void update(Long userId, ReceiveAddressParam param);

	List<ReceiveAddressParam> list(Long userId);

	ReceiveAddressParam detail(Long userId, Long id);
}
