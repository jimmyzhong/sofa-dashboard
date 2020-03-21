package me.izhong.shop.bid.frame;


import me.izhong.common.exception.BusinessException;

public interface IFilterChain {
	public void process(BidContext context, IFilterCallback callback)
			throws BusinessException;
}
