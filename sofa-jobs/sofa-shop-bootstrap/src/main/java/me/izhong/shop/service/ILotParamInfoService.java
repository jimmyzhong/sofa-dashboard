package me.izhong.shop.service;

import me.izhong.shop.entity.LotParamInfo;

public interface ILotParamInfoService {

    void saveOrUpdate(LotParamInfo lotParamInfo);

	LotParamInfo findById(Long id);
}
