package me.izhong.shop.service.mng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopAppVersionsMngFacade;
import me.izhong.jobs.model.ShopAppVersions;
import me.izhong.shop.dao.AppVersionsDao;
import me.izhong.shop.entity.AppVersions;
import me.izhong.shop.service.IAppVersionsService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopAppVersionsMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopAppVersionsMngFacadeImpl implements IShopAppVersionsMngFacade {
	
	@Autowired
	private AppVersionsDao appVersionsDao;

	@Autowired
	private IAppVersionsService appVersionsService;

	@Override
	public void create(ShopAppVersions shopAppVersions) {
		AppVersions version = new AppVersions();
		BeanUtils.copyProperties(shopAppVersions, version);
		version.setCreateTime(LocalDateTime.now());
		version.setUpdateTime(LocalDateTime.now());
		appVersionsService.saveOrUpdate(version);
	}

	@Override
	public void edit(ShopAppVersions shopVersions) {
		AppVersions version = appVersionsService.findById(shopVersions.getId());
		version.setType(shopVersions.getType());
		version.setVersion(shopVersions.getVersion());
		version.setDescription(shopVersions.getDescription());
		version.setUrl(shopVersions.getUrl());
		version.setForceUpdateVersion(shopVersions.getForceUpdateVersion());
		version.setUpdateTime(LocalDateTime.now());
		appVersionsService.saveOrUpdate(version);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				appVersionsService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopAppVersions> pageList(PageRequest request, String type) {
		AppVersions version = new AppVersions();
		if (!StringUtils.isEmpty(type)) {
			version.setType(type);
		}

        Example<AppVersions> example = Example.of(version);
		Page<AppVersions> page = appVersionsDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopAppVersions> list = page.getContent().stream().map(t -> {
        	ShopAppVersions obj = new ShopAppVersions();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopAppVersions find(Long adId) {
		AppVersions version = appVersionsService.findById(adId);
		ShopAppVersions shopVersion = new ShopAppVersions();
        BeanUtils.copyProperties(version, shopVersion);
        return shopVersion;
	}
}
