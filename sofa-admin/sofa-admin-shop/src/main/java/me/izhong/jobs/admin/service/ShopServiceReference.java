package me.izhong.jobs.admin.service;

import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;

import me.izhong.jobs.manage.IShopAdMngFacade;
import me.izhong.jobs.manage.IShopArticlesMngFacade;
import me.izhong.jobs.manage.IShopGoodsCategoryMngFacade;
import me.izhong.jobs.manage.IShopGoodsMngFacade;
import me.izhong.jobs.manage.IShopNoticeMngFacade;
import me.izhong.jobs.manage.IShopOrderMngFacade;
import me.izhong.jobs.manage.IShopReceiveAddressMngFacade;
import me.izhong.jobs.manage.IShopRegionMngFacade;
import me.izhong.jobs.manage.IShopTemplateMngFacade;
import me.izhong.jobs.manage.IShopUserMngFacade;
import me.izhong.jobs.manage.IShopVersionsMngFacade;

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

    @SofaReference(interfaceType = IShopOrderMngFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopOrderMngFacade orderService;

    @SofaReference(interfaceType = IShopReceiveAddressMngFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopReceiveAddressMngFacade receiveAddressService;

    @SofaReference(interfaceType = IShopRegionMngFacade.class,
    		uniqueId = "${service.unique.id}",
    		jvmFirst = false,
    		binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopRegionMngFacade regionService;

    @SofaReference(interfaceType = IShopAdMngFacade.class,
    		uniqueId = "${service.unique.id}",
    		jvmFirst = false,
    		binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopAdMngFacade adService;

    @SofaReference(interfaceType = IShopArticlesMngFacade.class,
    		uniqueId = "${service.unique.id}",
    		jvmFirst = false,
    		binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopArticlesMngFacade articlesService;

    @SofaReference(interfaceType = IShopNoticeMngFacade.class,
    		uniqueId = "${service.unique.id}",
    		jvmFirst = false,
    		binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopNoticeMngFacade noticeService;

    @SofaReference(interfaceType = IShopTemplateMngFacade.class,
    		uniqueId = "${service.unique.id}",
    		jvmFirst = false,
    		binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopTemplateMngFacade templateService;

    @SofaReference(interfaceType = IShopVersionsMngFacade.class,
    		uniqueId = "${service.unique.id}",
    		jvmFirst = false,
    		binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    public IShopVersionsMngFacade versionService;
}
