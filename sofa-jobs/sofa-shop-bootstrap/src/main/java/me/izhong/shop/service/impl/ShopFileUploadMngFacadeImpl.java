package me.izhong.shop.service.impl;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.manage.IShopFileUploadMngFacade;

@Slf4j
@Service
@SofaService(interfaceType = IShopFileUploadMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopFileUploadMngFacadeImpl implements IShopFileUploadMngFacade {

    @Autowired
    private ThirdPartyService thirdService;

    @Override
    public String uploadFile(Map<String, Object> map) {
    	String fileName = UUID.randomUUID().toString().replace("-", "");
    	MultipartFile file = (MultipartFile) map.get("file");
        return thirdService.uploadFile(fileName, file);
    }
}
