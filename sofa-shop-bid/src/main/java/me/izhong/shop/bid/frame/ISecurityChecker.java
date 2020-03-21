package me.izhong.shop.bid.frame;

import me.izhong.common.exception.BusinessException;


public interface ISecurityChecker {

	public void checkSign(BidContext context) throws BusinessException;


}
