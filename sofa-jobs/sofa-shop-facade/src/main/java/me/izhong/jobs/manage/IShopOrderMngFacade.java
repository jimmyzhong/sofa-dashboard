package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopOrder;

public interface IShopOrderMngFacade {

	ShopOrder find(Long orderId);

    void updateOrderStatus(List<Long> ids, Integer orderStatus);

    PageModel<ShopOrder> pageList(PageRequest request, ShopOrder order);

    boolean remove(Long orderId);
}
