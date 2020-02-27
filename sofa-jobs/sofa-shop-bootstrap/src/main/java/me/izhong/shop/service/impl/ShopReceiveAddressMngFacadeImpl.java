package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.manage.IShopReceiveAddressMngFacade;
import me.izhong.jobs.model.ShopReceiveAddress;
import me.izhong.shop.dao.UserReceiveAddressDao;
import me.izhong.shop.entity.UserReceiveAddress;

@Slf4j
@Service
@SofaService(interfaceType = IShopReceiveAddressMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopReceiveAddressMngFacadeImpl implements IShopReceiveAddressMngFacade {

	@Autowired
	private UserReceiveAddressDao userReceiveAddressDao;

	@Override
	public PageModel<ShopReceiveAddress> pageList(PageRequest request, String phone, String name) {
		UserReceiveAddress address = new UserReceiveAddress();
		address.setPhone(phone);
		address.setName(name);

        Example<UserReceiveAddress> example = Example.of(address);
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getIsAsc())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
                    request.getOrderByColumn());
        }

        Pageable pageableReq = of(
                Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
        Page<UserReceiveAddress> page = userReceiveAddressDao.findAll(example, pageableReq);
        List<ShopReceiveAddress> shopGoodList = page.getContent().stream().map(t -> {
        	ShopReceiveAddress obj = new ShopReceiveAddress();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), shopGoodList);
	}

}
