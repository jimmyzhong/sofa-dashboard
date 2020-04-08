package me.izhong.shop.service.mng;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.manage.IShopOrderItemMngFacade;
import me.izhong.jobs.model.ShopOrderItem;
import me.izhong.shop.dao.OrderItemDao;
import me.izhong.shop.entity.OrderItem;

@Slf4j
@Service
@SofaService(interfaceType = IShopOrderItemMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopOrderItemMngFacadeImpl implements IShopOrderItemMngFacade {
	
	@Autowired
	private OrderItemDao orderItemDao;

	@Override
	public List<ShopOrderItem> query(Long orderId) {
		List<OrderItem> orderItems = orderItemDao.findAllByOrderId(orderId);
		if (!CollectionUtils.isEmpty(orderItems)) {
			return orderItems.stream().map(t -> {
				ShopOrderItem shopOrderItem = new ShopOrderItem();
				BeanUtils.copyProperties(t, shopOrderItem);
				return shopOrderItem;
			}).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}
}
