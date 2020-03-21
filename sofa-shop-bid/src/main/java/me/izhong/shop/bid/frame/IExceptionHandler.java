package me.izhong.shop.bid.frame;

public interface IExceptionHandler {

	void handleException(BidContext context, Throwable t);

	void handleException(BidContext context, int errCode, String errInfo);

	void addCleanupListener(ExceptionCleanupListener l);

	boolean removeCleanupListener(ExceptionCleanupListener l);
}