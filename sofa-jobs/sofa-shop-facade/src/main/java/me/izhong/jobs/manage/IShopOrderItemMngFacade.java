package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.jobs.model.ShopOrderItem;

public interface IShopOrderItemMngFacade {

    List<ShopOrderItem> query(Long orderId);
}
