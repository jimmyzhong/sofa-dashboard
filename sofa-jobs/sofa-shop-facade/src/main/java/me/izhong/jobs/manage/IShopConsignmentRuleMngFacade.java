package me.izhong.jobs.manage;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopConsignmentRule;

public interface IShopConsignmentRuleMngFacade {

    void create(ShopConsignmentRule shopConsignmentRule);

    void edit(ShopConsignmentRule shopConsignmentRule);

    boolean remove(String ids);

	PageModel<ShopConsignmentRule> pageList(PageRequest fromRequest);

	ShopConsignmentRule find(Long id);
}
