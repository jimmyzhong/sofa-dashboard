package me.izhong.shop.bid.frame;


public interface ExceptionCleanupListener {
	public void cleanup(BidContext context, Throwable e);
}
