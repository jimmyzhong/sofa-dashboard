package me.izhong.shop.service;

import me.izhong.shop.entity.Lots;

public interface ILotsService {

    void saveOrUpdate(Lots lots);

	void deleteById(Long id);

	Lots findById(Long id);
}
