package me.izhong.shop.bid.frame;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.constant.ErrCode;
import me.izhong.common.exception.BusinessException;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceRegistry {

	private static ConcurrentHashMap<String, BusinessNode> registry = new ConcurrentHashMap<>();

	public static final String DEFAULT_SERVICE = "*";

	static private BusinessNode defaultService;

	public static void addService(String url, BusinessNode service) {
		if (DEFAULT_SERVICE.equals(url)) {
			defaultService = service;
			//log.info("注册默认服务: {}", service.getName());
			return;
		}

		if (url == null || url.length() == 0)
			throw new IllegalArgumentException("msgType cannot be null");
		String key = url;

		BusinessNode existingService = registry.get(key);
		if (existingService != null) {
			//log.warn("服务{}跟已注册服务{}冲突，key={}。", service.getName(),
			//		existingService.getName(), key);
		}
		registry.put(key, service);

		//log.info("注册服务: {}, {}", key, service.getName());
	}

	public static BusinessNode getService(String url)
			throws BusinessException {
		if (url == null || url.length() == 0)
			throw new IllegalArgumentException("url cannot be null");
		String key = null;
		BusinessNode service = null;
		key = url;
		service = registry.get(key);
		if (service != null)
			return service;

		if (defaultService != null)
			return defaultService;

		throw new BusinessException(ErrCode.FAIL_CODE, "系统不支持该业务");
	}

	public static Collection<BusinessNode> getAllServices() {
		return registry.values();
	}

	public static Set<String> getAllMsgTypes() {
		return registry.keySet();
	}
}
