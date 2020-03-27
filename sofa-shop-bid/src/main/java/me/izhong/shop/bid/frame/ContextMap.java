package me.izhong.shop.bid.frame;

import java.util.HashMap;

public class ContextMap extends HashMap<String, Object> {
	public String getString(String key) {
		return (String) get(key);
	}
	public Boolean getBoolean(String key) {
		return (Boolean) get(key);
	}
	public Long getLong(String key) { return (Long) get(key); }

	public Integer getInteger(String key) {
		return (Integer) get(key);
	}

	public Object getObject(String key) {
		return get(key);
	}
}
