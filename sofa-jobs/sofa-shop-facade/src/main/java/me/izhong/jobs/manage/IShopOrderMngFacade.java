package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.dto.OrderDeliveryParam;
import me.izhong.jobs.dto.OrderQueryParam;
import me.izhong.jobs.dto.ReceiverInfoParam;
import me.izhong.jobs.model.ShopOrder;

public interface IShopOrderMngFacade {

    PageModel<ShopOrder> pageList(PageRequest request, OrderQueryParam param);

    PageModel<ShopOrder> pageList(PageRequest request,Long userId, OrderQueryParam param);

	void delivery(List<OrderDeliveryParam> deliveryParamList);

	void close(List<Long> ids, String note);

	void delete(List<Long> ids);

	ShopOrder detail(Long id);

	void updateReceiverInfo(ReceiverInfoParam receiverInfoParam);

	void updateNote(Long id, String note, Integer status);
}
