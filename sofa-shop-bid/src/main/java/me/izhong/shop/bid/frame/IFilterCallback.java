package me.izhong.shop.bid.frame;

import me.izhong.common.exception.BusinessException;

public interface IFilterCallback {
	void onPostProcess(BidContext context) throws BusinessException;
}
