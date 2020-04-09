package me.izhong.shop.service;

import me.izhong.shop.entity.VipInfo;

public interface IVipInfoService {

    void saveOrUpdate(VipInfo vipInfo);

	void deleteById(Long id);

	VipInfo findById(Long id);
}
