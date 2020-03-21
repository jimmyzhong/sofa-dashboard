package me.izhong.shop.bid.frame;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.pojo.ErrorResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
abstract public class AbstractExceptionHandler implements IExceptionHandler {

	private List<ExceptionCleanupListener> listners = new ArrayList<ExceptionCleanupListener>();

	public AbstractExceptionHandler() {
		super();
	}

	@Override
	public void addCleanupListener(ExceptionCleanupListener l) {
		listners.add(l);
	}

	@Override
	public boolean removeCleanupListener(ExceptionCleanupListener l) {
		return listners.remove(l);
	}

	protected void cleanup(BidContext context, Throwable t) {
		List<ExceptionCleanupListener> copyOfListeners = new ArrayList<>(listners);

		for (ExceptionCleanupListener l : copyOfListeners) {
			try {
				l.cleanup(context, t);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void handleException(BidContext context, Throwable t) {
		try {
			if (context == null) {
				context = new BidContext();
			}
			context.setProcessException(t);
			cleanup(context, t);

			ErrorResponse resp = null;

			if (t instanceof BusinessException) {
				BusinessException e = (BusinessException) t;
				log.error("业务错误{}: {}", e.getCode(), e.getMessage());
				resp = new ErrorResponse(context, e.getCode(),
						e.getMessage());
			} else {
				log.error("异常：", t);
				resp = new ErrorResponse(context,
						400, "系统错误");
			}

			String msg = JSON.toJSONString(resp);
			context.setJsonResponse(msg);
			context.setResponse(resp);

			response(context);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void handleException(BidContext context, int code,
			String errInfo) {
		try {
			if (context == null) {
				context = new BidContext();
			}

			Exception t = new BusinessException(code, errInfo);
			context.setProcessException(t);

			cleanup(context, t);

			log.error(errInfo);
			ErrorResponse resp = new ErrorResponse(context, code, errInfo);

			String msg = JSON.toJSONString(resp);
			context.setJsonResponse(msg);
			context.setResponse(resp);
			response(context);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	abstract protected void response(BidContext context);

}