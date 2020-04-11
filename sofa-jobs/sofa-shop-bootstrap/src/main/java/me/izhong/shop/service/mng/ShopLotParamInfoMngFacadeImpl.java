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
import me.izhong.jobs.manage.IShopLotParamInfoMngFacade;
import me.izhong.jobs.model.ShopLotParamInfo;
import me.izhong.shop.dao.LotParamInfoDao;
import me.izhong.shop.entity.LotParamInfo;
import me.izhong.shop.service.ILotParamInfoService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopLotParamInfoMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopLotParamInfoMngFacadeImpl implements IShopLotParamInfoMngFacade {

	@Autowired
	private LotParamInfoDao lotParamInfoDao;
	@Autowired
	private ILotParamInfoService lotParamInfoService;

	@Override
	public void create(ShopLotParamInfo shopLotParamInfo) {
		LotParamInfo lotParamInfo = new LotParamInfo();
		BeanUtils.copyProperties(shopLotParamInfo, lotParamInfo);
		lotParamInfoService.saveOrUpdate(lotParamInfo);
	}

	@Override
	public void edit(ShopLotParamInfo shopLotParamInfo) {
		LotParamInfo lotParamInfo = lotParamInfoService.findById(shopLotParamInfo.getId());
		lotParamInfo.setType(shopLotParamInfo.getType());
		lotParamInfoService.saveOrUpdate(lotParamInfo);
	}

	@Override
	public PageModel<ShopLotParamInfo> pageList(PageRequest request) {
		LotParamInfo lotParamInfo = new LotParamInfo();
		Example<LotParamInfo> example = Example.of(lotParamInfo);
        Page<LotParamInfo> page = lotParamInfoDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopLotParamInfo> list = page.getContent().stream().map(t -> {
        	ShopLotParamInfo obj = new ShopLotParamInfo();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopLotParamInfo find(Long id) {
		LotParamInfo lotParamInfo = lotParamInfoService.findById(id);
		ShopLotParamInfo obj = new ShopLotParamInfo();
        BeanUtils.copyProperties(lotParamInfo, obj);
        return obj;
	}
}
