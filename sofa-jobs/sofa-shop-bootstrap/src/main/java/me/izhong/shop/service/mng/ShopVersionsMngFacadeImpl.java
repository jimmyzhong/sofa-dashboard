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

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.manage.IShopVersionsMngFacade;
import me.izhong.jobs.model.ShopVersions;
import me.izhong.shop.dao.VersionsDao;
import me.izhong.shop.entity.Versions;
import me.izhong.shop.service.IVersionsService;
import me.izhong.shop.util.PageableConvertUtil;

@Service
@SofaService(interfaceType = IShopVersionsMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopVersionsMngFacadeImpl implements IShopVersionsMngFacade {
	
	@Autowired
	private VersionsDao versionsDao;

	@Autowired
	private IVersionsService versionsService;

	@Override
	public void create(ShopVersions shopVersions) {
		Versions version = new Versions();
		BeanUtils.copyProperties(shopVersions, version);
		version.setCreateTime(LocalDateTime.now());
		version.setUpdateTime(LocalDateTime.now());
		versionsService.saveOrUpdate(version);
	}

	@Override
	public void edit(ShopVersions shopVersions) {
		Versions version = versionsService.findById(shopVersions.getId());
		version.setType(shopVersions.getType());
		version.setVersion(shopVersions.getVersion());
		version.setDesc(shopVersions.getDesc());
		version.setUrl(shopVersions.getUrl());
		version.setForceUpdateVersion(shopVersions.getForceUpdateVersion());
		version.setUpdateTime(LocalDateTime.now());
		versionsService.saveOrUpdate(version);
	}


	@Override
	public PageModel<ShopVersions> pageList(PageRequest request, String type) {
		Versions version = new Versions();
		if (!StringUtils.isEmpty(type)) {
			version.setType(type);
		}

        Example<Versions> example = Example.of(version);
		Page<Versions> page = versionsDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopVersions> shopGoodCategoryList = page.getContent().stream().map(t -> {
        	ShopVersions obj = new ShopVersions();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), shopGoodCategoryList);
	}



	@Override
	public ShopVersions find(Long adId) {
		Versions version = versionsService.findById(adId);
		ShopVersions shopVersion = new ShopVersions();
        BeanUtils.copyProperties(version, shopVersion);
        return shopVersion;
	}
}
