package me.izhong.shop.bid.frame;

import me.izhong.common.exception.BusinessException;
import org.springframework.util.Assert;

import java.util.LinkedList;

public class LinkedFilterChain implements IFilterChain {
	private LinkedList<IProcessFilter> filters;

	public LinkedFilterChain(LinkedList<IProcessFilter> filters) {
		this.filters = filters;
	}

	@Override
	public void process(BidContext context, IFilterCallback callback)
			throws BusinessException {
		Assert.isTrue(filters != null && !filters.isEmpty());
		Assert.isTrue(callback != null);

		IProcessFilter nextFilter = filters.removeFirst();
		nextFilter.process(context, callback, this);
	}

	public LinkedList<IProcessFilter> getFilters() {
		return new LinkedList<IProcessFilter>(filters);
	}

}
