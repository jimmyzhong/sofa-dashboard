package me.izhong.shop.bid.action;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.IFilterCallback;
import me.izhong.shop.bid.frame.IFilterChain;
import me.izhong.shop.bid.frame.IProcessFilter;

public  interface IActionNode extends IProcessFilter {

    void process(BidContext context, IFilterCallback callback) throws BusinessException;

    default void process(BidContext context, IFilterCallback callback,
                        IFilterChain filterChain) throws BusinessException {
        process(context,callback);
    }

}
