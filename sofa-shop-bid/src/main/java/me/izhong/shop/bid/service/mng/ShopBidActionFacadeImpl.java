package me.izhong.shop.bid.service.mng;


import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.manage.IShopBidActionFacade;
import me.izhong.jobs.model.bid.BidResultInfo;
import me.izhong.jobs.model.bid.BidUploadInfo;
import me.izhong.shop.bid.pojo.BidQueryItem;
import me.izhong.shop.bid.service.RedisUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@SofaService(interfaceType = IShopBidActionFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopBidActionFacadeImpl implements IShopBidActionFacade {

    @Autowired
    private RedisUtilService redisUtilService;

    @Override
    public Boolean uploadBid(BidUploadInfo bid) {
        return null;
    }

    @Override
    public BidResultInfo downloadBid(Long bidId, Long startIndex, Long max) {
        if(bidId == null)
            throw BusinessException.build("bidId不能为空");
        if(startIndex == null)
            throw BusinessException.build("startIndex不能为空");
        if(max == null)
            max = Long.MAX_VALUE;
        List<BidQueryItem> items = redisUtilService.poolBidItems(bidId.toString(), startIndex,max);


        return null;
    }
}

