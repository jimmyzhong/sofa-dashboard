package me.izhong.jobs.admin.service;

import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;

import me.izhong.jobs.manage.IShopGoodsCategoryMngFacade;
import me.izhong.jobs.manage.IShopGoodsMngFacade;
import me.izhong.jobs.manage.IShopUserMngFacade;

@Service
public class ShopServiceReference {

    @SofaReference(interfaceType = IShopUserMngFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopUserMngFacade userService;

    @SofaReference(interfaceType = IShopGoodsMngFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopGoodsMngFacade goodsService;

    @SofaReference(interfaceType = IShopGoodsCategoryMngFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopGoodsCategoryMngFacade goodsCategoryService;
}
