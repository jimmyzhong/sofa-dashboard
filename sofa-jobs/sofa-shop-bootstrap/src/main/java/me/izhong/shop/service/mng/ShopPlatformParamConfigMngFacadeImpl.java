package me.izhong.shop.service.mng;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.manage.IPlatformParamConfigMngFacade;
import me.izhong.jobs.model.ShopPlatformParamConfig;
import me.izhong.shop.dao.PlatformParamConfigDao;
import me.izhong.shop.entity.PlatformParamConfig;
import me.izhong.shop.service.IPlatformParamConfigService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IPlatformParamConfigMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopPlatformParamConfigMngFacadeImpl implements IPlatformParamConfigMngFacade {

	@Autowired
	private PlatformParamConfigDao platformParamConfigDao;
	@Autowired
	private IPlatformParamConfigService platformParamConfigService;

	@Override
	public void create(ShopPlatformParamConfig shopPlatformParamConfig) {
		PlatformParamConfig config = new PlatformParamConfig();
		BeanUtils.copyProperties(shopPlatformParamConfig, config);
		platformParamConfigService.saveOrUpdate(config);
	}

	@Override
	public void edit(ShopPlatformParamConfig shopPlatformParamConfig) {
		PlatformParamConfig config = platformParamConfigService.findById(shopPlatformParamConfig.getId());
		config.setConfigValue(shopPlatformParamConfig.getConfigValue());
		config.setDescription(shopPlatformParamConfig.getDescription());
		platformParamConfigService.saveOrUpdate(config);
	}

	@Override
	public PageModel<ShopPlatformParamConfig> pageList(PageRequest request) {
		PlatformParamConfig config = new PlatformParamConfig();

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains());
		Example<PlatformParamConfig> example = Example.of(config, matcher);

        Page<PlatformParamConfig> page = platformParamConfigDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopPlatformParamConfig> list = page.getContent().stream().map(t -> {
        	ShopPlatformParamConfig obj = new ShopPlatformParamConfig();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopPlatformParamConfig find(Long id) {
		PlatformParamConfig config = platformParamConfigService.findById(id);
		ShopPlatformParamConfig obj = new ShopPlatformParamConfig();
        BeanUtils.copyProperties(config, obj);
        return obj;
	}
}
