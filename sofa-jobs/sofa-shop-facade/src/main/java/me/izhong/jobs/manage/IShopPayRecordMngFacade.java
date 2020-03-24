package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopPayRecord;

public interface IShopPayRecordMngFacade {

	PageModel<ShopPayRecord> pageList(PageRequest request, Long userId, List<Integer> moneyTypes);
}
