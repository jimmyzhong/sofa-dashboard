package me.izhong.shop.bid.action;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.IFilterCallback;
import me.izhong.shop.bid.frame.IFilterChain;

public interface IActionNode {

    public void process(BidContext context, IFilterCallback callback) throws BusinessException;

}
