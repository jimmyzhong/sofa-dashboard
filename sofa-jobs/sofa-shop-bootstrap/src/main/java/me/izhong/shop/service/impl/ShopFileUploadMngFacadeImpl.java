package me.izhong.shop.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.AliOssUploadUtil;
import me.izhong.jobs.manage.IShopFileUploadMngFacade;
import me.izhong.shop.config.AliCloudProperties;

@Slf4j
@Service
@SofaService(interfaceType = IShopFileUploadMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopFileUploadMngFacadeImpl implements IShopFileUploadMngFacade {

    @Autowired
    private AliCloudProperties properties;

    @Override
    public String uploadFile(Map<String, Object> map) throws Exception {
    	String fileName = UUID.randomUUID().toString().replace("-", "");
    	byte[] b = (byte[]) map.get("fileBytes");
    	String c = (String) map.get("contentType");
    	return AliOssUploadUtil.putOssObj(properties.getOssAccessKey(), properties.getOssAccessSecret(),
    			properties.getOssPicBucket(), properties.getOssEndpoint(), "/"+fileName + ".jpg", b, c);
    }
}
