package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopOrder;
import me.izhong.jobs.model.ShopReceiverInfo;

public interface IShopOrderMngFacade {

	ShopOrder find(Long orderId);

    void updateOrderStatus(List<Long> ids, Integer orderStatus);

	void updateReceiverInfo(ShopReceiverInfo shopReceiverInfo);

	void updateNote(Long id, String note);

    PageModel<ShopOrder> pageList(PageRequest request, ShopOrder order);

    boolean remove(Long orderId);


}
