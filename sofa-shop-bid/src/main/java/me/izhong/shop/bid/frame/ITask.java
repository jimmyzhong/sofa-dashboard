package me.izhong.shop.bid.frame;


import me.izhong.common.exception.BusinessException;

public interface ITask {
	public void run() throws BusinessException;
}
