package me.izhong.shop.bid.frame;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.action.IActionNode;
import me.izhong.shop.bid.pojo.BaseRequest;
import me.izhong.shop.bid.pojo.BaseResponse;
import me.izhong.shop.bid.pojo.BidResponse;
import org.apache.commons.lang3.AnnotationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BusinessNode {

	@Setter
	@Getter
	private List<IProcessFilter> filterTemplate;

	@Autowired
	private List<IActionNode> actionNodes;

	private Map<String,IActionNode> actionNodeMaps;

	public void execute(BidContext context, IFilterCallback lastCallback)
			throws BusinessException {

		log.info("开始处理业务请求: {}", context.getUrl());

		context.setBusinessNode(this);

		// 初始化责任链
		LinkedList<IProcessFilter> filters = new LinkedList<IProcessFilter>();
		if (filterTemplate != null) {
			filters.addAll(filterTemplate);
		}

		ServiceRegistry.getService(context.getUrl());

		//filters.add(filter);
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

		});
	}

}
