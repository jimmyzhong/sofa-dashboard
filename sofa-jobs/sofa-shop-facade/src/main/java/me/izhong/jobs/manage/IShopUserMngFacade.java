package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopUser;

/**
 * 定义返回管理平台的用户接口
 */
public interface IShopUserMngFacade {

    ShopUser find(Long userId);

    boolean disable(Long userId);

    boolean enable(Long userId);

    ShopUser edit(ShopUser user);

    PageModel<ShopUser> pageList(PageRequest request, ShopUser shopUser);

	boolean remove(String ids);
}
