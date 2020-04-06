package me.izhong.shop.service.mng;

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
import me.izhong.jobs.manage.IShopLotsItemMngFacade;
import me.izhong.jobs.model.ShopLotsItem;
import me.izhong.shop.dao.LotsItemDao;
import me.izhong.shop.entity.LotsItem;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopLotsItemMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopLotsItemMngFacadeImpl implements IShopLotsItemMngFacade {

	@Autowired
	private LotsItemDao lotsItemDao;

	@Override
	public PageModel<ShopLotsItem> pageList(PageRequest request, Long auctionId) {
		LotsItem lotsItem = new LotsItem();
		if (auctionId != null) {
			lotsItem.setLotsId(auctionId);
		}
        Example<LotsItem> example = Example.of(lotsItem);
		Page<LotsItem> page = lotsItemDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopLotsItem> list = page.getContent().stream().map(t -> {
        	ShopLotsItem obj = new ShopLotsItem();
            BeanUtils.copyProperties( t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}
}
