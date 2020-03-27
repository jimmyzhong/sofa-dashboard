package me.izhong.shop.bid.frame;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.action.IActionNode;
import me.izhong.shop.bid.ann.ActionNode;
import me.izhong.shop.bid.filter.JsonConvertFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BusinessNode {

	@Setter
	@Getter
	private List<IProcessFilter> filterTemplate;

	@Autowired
	private List<IActionNode> actionNodes;

	@Autowired
	private JsonConvertFilter jsonConvertFilter;

	private ConcurrentHashMap<String,IActionNode> actionNodeMaps = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String,Class> actionNodeClassMaps = new ConcurrentHashMap<>();

	public void execute(BidContext context, IFilterCallback lastCallback)
			throws BusinessException {

		String url = context.getUrl();
		log.info("开始处理业务请求: {}", url);
		if(StringUtils.isBlank(url))
			throw BusinessException.build("url不能为空");

		IActionNode node = actionNodeMaps.get(url);
		if(node == null)
			throw BusinessException.build("请求服务未找到");

		context.setActionNode(node);
		Class reqClass = actionNodeClassMaps.get(url);
		context.setReqClass(reqClass);

		// 初始化责任链
		LinkedList<IProcessFilter> filters = new LinkedList<IProcessFilter>();
		if (filterTemplate != null) {
			filters.addAll(filterTemplate);
		}
		if(reqClass != null && !reqClass.equals(Object.class))
			filters.add(jsonConvertFilter);

		filters.add(node);
		LinkedFilterChain filterChain = new LinkedFilterChain(filters);

		try {
			filterChain.process(context, lastCallback);
		} finally {
			//log.info("完成业务请求: {}", getName());
		}
	}

	@PostConstruct
	public void init() {
		actionNodes.forEach(e -> {
			ActionNode an = e.getClass().getAnnotation(ActionNode.class);
			String url = an.url();
			String name = an.name();
			Class tClass = an.reqClass();
			log.info("注册{} {}",url,name);
			actionNodeMaps.put(url,e);
			actionNodeClassMaps.put(url,tClass);
		});
	}

}
