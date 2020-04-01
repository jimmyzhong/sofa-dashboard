package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopSuppliers;

public interface IShopSuppliersMngFacade {

    void create(ShopSuppliers shopSuppliers);

    void edit(ShopSuppliers shopSuppliers);

    boolean remove(String ids);

	PageModel<ShopSuppliers> pageList(PageRequest fromRequest, String name);

	ShopSuppliers find(Long id);
}
