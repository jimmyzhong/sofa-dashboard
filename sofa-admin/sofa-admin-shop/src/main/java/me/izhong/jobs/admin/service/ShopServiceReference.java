package me.izhong.jobs.admin.service;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.manage.IShopUserMngFacade;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceReference {

    @SofaReference(interfaceType = IShopUserMngFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt",timeout = 20000))
    public IShopUserMngFacade userService;

}
