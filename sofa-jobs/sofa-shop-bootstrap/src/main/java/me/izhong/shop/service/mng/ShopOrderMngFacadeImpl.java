package me.izhong.shop.service.mng;

import static org.springframework.data.domain.PageRequest.of;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import me.izhong.shop.util.PageableConvertUtil;
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
import me.izhong.jobs.dto.OrderDeliveryParam;
import me.izhong.jobs.dto.OrderQueryParam;
import me.izhong.jobs.dto.ReceiverInfoParam;
import me.izhong.jobs.manage.IShopOrderMngFacade;
import me.izhong.jobs.model.ShopOrder;
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
	public PageModel<ShopOrder> pageList(PageRequest request, OrderQueryParam param) {
		Order order = new Order();
		BeanUtils.copyProperties(param, order);
        Example<Order> example = Example.of(order);

        Page<Order> page = orderDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopOrder> shopGoodCategoryList = page.getContent().stream().map(t -> {
        	ShopOrder obj = new ShopOrder();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), shopGoodCategoryList);
	}

	@Override
	public void delivery(List<OrderDeliveryParam> deliveryParamList) {
		orderService.delivery(deliveryParamList);
	}

	@Override
	public void close(List<Long> ids, String note) {
		Order order = new Order();
		order.setStatus(4);
		order.setUpdateTime(LocalDateTime.now());
		orderService.update(order);
	}

	@Override
	public void delete(List<Long> ids) {
		Order order = new Order();
		order.setIsDelete(1);
		order.setUpdateTime(LocalDateTime.now());
		orderService.update(order);
	}

	@Override
	public ShopOrder detail(Long id) {
		Order order = orderService.findById(id);
		ShopOrder shopOrder = new ShopOrder();
		BeanUtils.copyProperties(order, shopOrder);
		return shopOrder;
	}

	@Override
	public void updateReceiverInfo(ReceiverInfoParam receiverInfoParam) {
		Order order = new Order();
        order.setId(receiverInfoParam.getOrderId());
        order.setReceiverName(receiverInfoParam.getReceiverName());
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());
        order.setReceiverCity(receiverInfoParam.getReceiverCity());
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());
		order.setUpdateTime(LocalDateTime.now());
		orderService.update(order);
	}

	@Override
	public void updateNote(Long id, String note, Integer status) {
		Order order = new Order();
		order.setId(id);
        order.setNote(note);
        order.setUpdateTime(LocalDateTime.now());
        orderService.update(order);
	}

}
