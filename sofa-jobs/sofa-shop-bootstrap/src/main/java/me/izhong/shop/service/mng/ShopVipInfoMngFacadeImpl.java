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
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopVipInfoMngFacade;
import me.izhong.jobs.model.ShopVipInfo;
import me.izhong.shop.dao.VipInfoDao;
import me.izhong.shop.entity.VipInfo;
import me.izhong.shop.service.IVipInfoService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopVipInfoMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopVipInfoMngFacadeImpl implements IShopVipInfoMngFacade {

	@Autowired
	private VipInfoDao vipInfoDao;
	@Autowired
	private IVipInfoService vipInfoService;

	@Override
	public void create(ShopVipInfo shopVipInfo) {
		VipInfo vipInfo = new VipInfo();
		BeanUtils.copyProperties(shopVipInfo, vipInfo);
		vipInfoService.saveOrUpdate(vipInfo);
	}

	@Override
	public void edit(ShopVipInfo shopVipInfo) {
		VipInfo vipInfo = vipInfoService.findById(shopVipInfo.getId());
		vipInfo.setLevel(shopVipInfo.getLevel());
		vipInfo.setName(shopVipInfo.getName());
		vipInfo.setPayAmt(shopVipInfo.getPayAmt());
		vipInfo.setGiftPoints(shopVipInfo.getGiftPoints());
		vipInfoService.saveOrUpdate(vipInfo);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				vipInfoService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopVipInfo> pageList(PageRequest request) {
		VipInfo vipInfo = new VipInfo();

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains());
		Example<VipInfo> example = Example.of(vipInfo, matcher);

        Page<VipInfo> page = vipInfoDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopVipInfo> list = page.getContent().stream().map(t -> {
        	ShopVipInfo obj = new ShopVipInfo();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopVipInfo find(Long id) {
		VipInfo vipInfo = vipInfoService.findById(id);
		ShopVipInfo obj = new ShopVipInfo();
        BeanUtils.copyProperties(vipInfo, obj);
        return obj;
	}
}
