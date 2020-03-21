package me.izhong.shop.bid.ntt;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.config.ConfigBean;
import me.izhong.shop.bid.config.ErrCode;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.BusinessNode;
import me.izhong.shop.bid.frame.IFilterCallback;
import me.izhong.shop.bid.frame.ServiceRegistry;
import me.izhong.shop.bid.pojo.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NettyInvokeService {
	@Autowired
	private ConfigBean configBean;
	public void processInvoke(final BidContext context, final Channel channel) {
		try {
			String url = context.getUrl();
			if (StringUtils.isBlank(url)) {
				throw new BusinessException(ErrCode.SYS_ERR, "url不能为空");
			}
			final BusinessNode service = ServiceRegistry.getService(url);

			service.execute(context, new IFilterCallback() {
				@Override
				public void onPostProcess(BidContext context) throws BusinessException {
					BaseResponse jsonString = context.getResponse();
					if (channel.isActive()) {
						channel.writeAndFlush(new BidMsg(context, JSON.toJSONString(jsonString)));
					}
//					log.info("完成异步业务应答: {}, 耗时: {}", service.getName(),
//							System.currentTimeMillis() - context.getServiceAcceptTime());
				}
			});
		} catch (Throwable e) {
			context.getExceptionHandler().handleException(context, e);
		}

		log.info("请求处理结束");
	}
}
