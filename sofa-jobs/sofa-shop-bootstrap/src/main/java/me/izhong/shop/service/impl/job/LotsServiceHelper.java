package me.izhong.shop.service.impl.job;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.DateUtil;
import me.izhong.jobs.manage.IShopBidActionFacade;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidUploadInfo;
import me.izhong.shop.dao.LotsDao;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.service.impl.LotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LotsServiceHelper {
    @Autowired
    LotsService service;
    @Autowired
    LotsDao lotsDao;
    @SofaReference(interfaceType = IShopBidActionFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    IShopBidActionFacade bidActionFacade;

    @PostConstruct
    public void setUp() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("lots-service-helper").build());

        scheduler.scheduleAtFixedRate(() -> {
            findLots(scheduler);
        }, 1, 30, TimeUnit.MINUTES);
    }

    private void findLots(ScheduledExecutorService scheduler) {
        LocalDateTime now = LocalDateTime.now();
        List<Lots> lotsList = lotsDao.findAllByStartTimeBetweenOrderByStartTime(now, now.plusMinutes(30));
        for (Lots lots : lotsList) {
            BidUploadInfo bid = convert2Bid(lots);
            bidActionFacade.uploadBid(bid);
            // already start
            if (lots.getStartTime().compareTo(LocalDateTime.now()) >= 0) {
                bidActionFacade.startBid(bid.getBidId());
            } else {
                scheduleBidStart(scheduler, lots.getStartTime(), bid);
            }

            schedulerBidEnd(scheduler, lots.getEndTime(),  bid);
        }
    }

    private void schedulerBidEnd(ScheduledExecutorService scheduler, LocalDateTime endTime, BidUploadInfo bid) {
        Long seconds = Duration.between(LocalDateTime.now(), endTime).getSeconds() + 5 * 60;
        scheduler.schedule(()->{
            endBid(bid);
        }, seconds, TimeUnit.SECONDS);
    }

    private void endBid(BidUploadInfo bid) {
        BidDownloadInfo info = bidActionFacade.downloadBid(bid.getBidId(), 1L, null);

    }

    private void scheduleBidStart(ScheduledExecutorService scheduler, LocalDateTime startTime, BidUploadInfo bid) {
        Long seconds = Duration.between(LocalDateTime.now(), startTime).getSeconds();
        scheduler.schedule(()->{
            startBid(bid);
        }, seconds, TimeUnit.SECONDS);
    }

    private void startBid(BidUploadInfo bid) {
        log.info("start bid " + bid.getBidId());
        boolean started = bidActionFacade.startBid(bid.getBidId());
        log.info("started. " + bid.getBidId() + "::"+ started);
    }

    private BidUploadInfo convert2Bid(Lots lots) {
        BidUploadInfo info = new BidUploadInfo();
        info.setBidId(lots.getId());
        info.setEndPrice(lots.getFinalPrice().multiply(BigDecimal.valueOf(100)).longValue());
        info.setEndTime(DateUtil.convertToDate(lots.getEndTime()));
        info.setStartPrice(lots.getStartPrice().multiply(BigDecimal.valueOf(100)).longValue());
        info.setStartTime(DateUtil.convertToDate(lots.getStartTime()));
        info.setStepPrice(lots.getAddPrice().multiply(BigDecimal.valueOf(100)).longValue());
        return info;
    }
}
