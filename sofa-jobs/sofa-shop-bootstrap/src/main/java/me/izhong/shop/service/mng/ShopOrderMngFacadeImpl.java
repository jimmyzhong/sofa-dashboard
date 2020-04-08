package me.izhong.shop.service.mng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.google.common.collect.Lists;

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
import me.izhong.shop.util.PageableConvertUtil;

@Service
@SofaService(interfaceType = IShopOrderMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopOrderMngFacadeImpl implements IShopOrderMngFacade {
	
	@Autowired
	private OrderDao orderDao;

	@Autowired
	private IOrderService orderService;

	@Override
	public PageModel<ShopOrder> pageList(PageRequest request, OrderQueryParam param) {
		Specification<Order> specification = getOrderQuerySpeci(param.getOrderType(), param.getOrderSn(), param.getStart(), param.getEnd(), param.getStatus(), param.getUserId());
		return getOrderPageModel(request, specification);
	}

    private Specification<Order> getOrderQuerySpeci(Integer orderType, String orderSn, LocalDateTime start, LocalDateTime end, Integer status, Long userId) {
        return (r, cq, cb) -> {
        	List<Predicate> predicates = Lists.newArrayList();
        	if (orderType != null) {
        		predicates.add(cb.equal(r.get("orderType"), orderType));
        	}
        	if (!StringUtils.isEmpty(orderSn)) {
        		predicates.add(cb.like(r.get("orderSn"), "%" + orderSn + "%"));
        	}
        	if (start != null) {
        		predicates.add(cb.greaterThan(r.get("createTime"), start));
        	}
        	if (end != null) {
        		predicates.add(cb.lessThanOrEqualTo(r.get("createTime"), end));
        	}
        	if (status != null) {
        		predicates.add(cb.equal(r.get("status"), status));
        	}
        	if (userId != null) {
        		predicates.add(cb.equal(r.get("userId"), userId));
        	}
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private PageModel<ShopOrder> getOrderPageModel(PageRequest pageRequest, Specification<Order> specification) {
    	Page<Order> page = orderDao.findAll(specification, PageableConvertUtil.toDataPageable(pageRequest));
    	System.out.println(JSON.toJSONString(page.getContent()));
        List<ShopOrder> list = page.getContent().stream().map(t -> {
        	ShopOrder shopOrder = new ShopOrder();
            BeanUtils.copyProperties(t, shopOrder);
            return shopOrder;
        }).collect(Collectors.toList());
        System.out.println(JSON.toJSONString(list));
        return PageModel.instance(page.getTotalElements(), list);
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
