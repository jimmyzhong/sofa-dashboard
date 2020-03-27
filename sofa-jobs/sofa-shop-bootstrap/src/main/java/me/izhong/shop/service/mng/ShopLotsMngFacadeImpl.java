package me.izhong.shop.service.mng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
import me.izhong.jobs.manage.IShopLotsMngFacade;
import me.izhong.jobs.model.ShopLots;
import me.izhong.shop.dao.LotsDao;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.service.ILotsService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopLotsMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopLotsMngFacadeImpl implements IShopLotsMngFacade {
	
	@Autowired
	private LotsDao lotsDao;

	@Autowired
	private ILotsService lotsService;

	@Override
	public void create(ShopLots shopLots) {
		Lots lots = new Lots();
		BeanUtils.copyProperties(shopLots, lots);
		lots.setCreateTime(LocalDateTime.now());
		lots.setUpdateTime(LocalDateTime.now());
		lotsService.saveOrUpdate(lots);
	}

	@Override
	public void edit(ShopLots shopLots) {
		Lots lots = lotsService.findById(shopLots.getId());
		lots.setName(shopLots.getName());
		lots.setDescription(shopLots.getDescription());
		lots.setStartPrice(shopLots.getStartPrice());
		lots.setAddPrice(shopLots.getAddPrice());
		lots.setIsRepublish(1);
		lots.setUpdateTime(LocalDateTime.now());
		lotsService.saveOrUpdate(lots);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				lotsService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopLots> pageList(PageRequest request) {
		Lots lots = new Lots();
        Example<Lots> example = Example.of(lots);
		Page<Lots> page = lotsDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopLots> list = page.getContent().stream().map(t -> {
        	ShopLots obj = new ShopLots();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopLots find(Long id) {
		Lots lots = lotsService.findById(id);
		ShopLots shopLots = new ShopLots();
        BeanUtils.copyProperties(lots, shopLots);
        return shopLots;
	}
}
