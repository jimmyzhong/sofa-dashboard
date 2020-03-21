package me.izhong.shop.bid.frame;

import me.izhong.common.exception.BusinessException;

public interface IProcessFilter {

	void process(BidContext context, IFilterCallback callback,
                        IFilterChain filterChain) throws BusinessException;
}
