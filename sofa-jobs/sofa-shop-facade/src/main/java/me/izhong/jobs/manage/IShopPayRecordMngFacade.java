package me.izhong.jobs.manage;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.model.ShopPayRecord;

public interface IShopPayRecordMngFacade {

	PageModel<ShopPayRecord> pageMoneyList(PageRequest request, Long userId, List<Integer> moneyTypes);

	PageModel<ShopPayRecord> pageScoreList(PageRequest request, Long userId);
}
