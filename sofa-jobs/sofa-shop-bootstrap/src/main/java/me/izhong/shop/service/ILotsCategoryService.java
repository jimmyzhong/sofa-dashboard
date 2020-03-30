package me.izhong.shop.service;

import me.izhong.shop.entity.LotsCategory;

public interface ILotsCategoryService {

    void saveOrUpdate(LotsCategory lotsCategory);

	void deleteById(Integer id);

	LotsCategory findById(Integer id);
}
