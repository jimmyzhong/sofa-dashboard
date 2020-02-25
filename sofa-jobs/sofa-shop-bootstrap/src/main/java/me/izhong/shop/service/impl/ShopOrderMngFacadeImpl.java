package me.izhong.shop.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.manage.IShopOrderMngFacade;
import me.izhong.jobs.model.ShopOrder;
import me.izhong.jobs.model.ShopReceiverInfo;
import me.izhong.shop.dao.OrderDao;
import me.izhong.shop.entity.Order;
import me.izhong.shop.service.IOrderService;

@Slf4j
@Service
@SofaService(interfaceType = IShopOrderMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopOrderMngFacadeImpl implements IShopOrderMngFacade {
	
	@Autowired
	private OrderDao orderDao;

	@Autowired
	private IOrderService orderService;

	@Override
	public ShopOrder find(Long orderId) {
		Order order = orderService.findById(orderId);
		ShopOrder shopOrder = new ShopOrder();
        BeanUtils.copyProperties(order, shopOrder);
        return shopOrder;
	}

	@Override
	public void updateOrderStatus(List<Long> ids, Integer orderStatus) {
		orderService.updateOrderStatusByIds(orderStatus, ids);
	}

	@Override
	public void updateReceiverInfo(ShopReceiverInfo shopReceiverInfo) {
		orderService.updateReceiverInfoById(shopReceiverInfo);
	}

	@Override
	@Transactional
	public void updateNote(Long id, String note) {
		orderService.updateNoteById(id, note);
	}

	@Override
	public PageModel<ShopOrder> pageList(PageRequest request, ShopOrder order) {
		return null;
	}

	@Override
	public boolean remove(Long orderId) {
		try {
			orderService.deleteById(orderId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
