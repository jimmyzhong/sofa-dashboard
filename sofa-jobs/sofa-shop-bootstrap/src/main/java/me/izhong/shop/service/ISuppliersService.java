package me.izhong.shop.service;

import me.izhong.shop.entity.Suppliers;

public interface ISuppliersService {

    void saveOrUpdate(Suppliers suppliers);

	void deleteById(Long id);

	Suppliers findById(Long id);
}
