package me.izhong.shop.service.impl.job;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.DateUtil;
import me.izhong.jobs.manage.IShopBidActionFacade;
import me.izhong.jobs.model.bid.BidDownloadInfo;
import me.izhong.jobs.model.bid.BidUploadInfo;
import me.izhong.jobs.model.bid.UserItem;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dao.*;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.impl.JobService;
import me.izhong.shop.service.impl.LotsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LotsServiceHelper {
    public static final String JOB_NAME = "lots-service-helper";
    @Autowired
    LotsService service;
    @Autowired
    LotsDao lotsDao;
    @Autowired
    UserDao userDao;
    @Autowired
    LotsItemDao lotsItemDao;
    @Autowired
    JobService jobService;

    @SofaReference(interfaceType = IShopBidActionFacade.class,
            uniqueId = "${service.unique.id}",
            jvmFirst = false,
            binding = @SofaReferenceBinding(bindingType = "bolt", timeout = 20000))
    IShopBidActionFacade bidActionFacade;

    @PostConstruct
    public void setUp() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat(JOB_NAME).build());


        scheduler.scheduleAtFixedRate(() -> {
            subscribeBids(scheduler);
        }, 1, 30, TimeUnit.SECONDS);
    }

    private void subscribeBids(ScheduledExecutorService scheduler) {
        LocalDateTime expect = LocalDateTime.now();
        if (!jobService.acquireJob(JOB_NAME, expect, expect.plusSeconds(30))) {
            log.warn("unable to acquire job " + JOB_NAME);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Lots> lotsList = lotsDao.findAllByStartTimeBetweenAndUploadedOrderByStartTime(now.minusHours(1),now.plusSeconds(30), false);
        log.info("prepare to upload bids size {}", lotsList == null ? 0 : lotsList.size());
        for (Lots lots : lotsList) {
            try {
                BidUploadInfo bid = convert2Bid(lots);
                bidActionFacade.uploadBid(bid);
                log.info("upload bid {} success.", bid.getBidId());
                schedulerBidEnd(scheduler, lots.getEndTime(), bid);
                service.markLotsAsUploaded(lots);
            }catch (Exception e) {
                log.error("schedule bid error.", e);
            }
        }
    }

    private void schedulerBidEnd(ScheduledExecutorService scheduler, LocalDateTime endTime, BidUploadInfo bid) {
        Long seconds = Duration.between(LocalDateTime.now(), endTime).getSeconds() + 5 * 60;
        scheduler.schedule(()->{
            endBid(bid);
        }, seconds, TimeUnit.SECONDS);
        log.info("subscribe bid end at {} seconds later", seconds.toString());
    }

    private void endBid(BidUploadInfo bid) {
        BidDownloadInfo info = bidActionFacade.downloadBid(bid.getBidId(), 1L, null);
        if (!info.getIsOver()) {
            log.warn("拍卖没有结束" + bid.getBidId());
        }
        service.saveLots(bid.getBidId(), info);
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
        info.setBidId(lots.getLotsNo());
        info.setEndPrice(lots.getWarningPrice().multiply(BigDecimal.valueOf(100)).longValue());
        info.setEndTime(DateUtil.convertToDate(lots.getEndTime()));
        info.setStartPrice(lots.getStartPrice().multiply(BigDecimal.valueOf(100)).longValue());
        info.setStartTime(DateUtil.convertToDate(lots.getStartTime()));
        info.setStepPrice(lots.getAddPrice().multiply(BigDecimal.valueOf(100)).longValue());

        List<User> userList = userDao.selectAcutionUsers(MoneyTypeEnum.AUCTION_MARGIN.getType(), lots.getId(),
                OrderStateEnum.PAID.getState());
        info.setUsers(userList.stream().map(u->{
            UserItem item = new UserItem();
            item.setUserId(u.getId());
            item.setAvatar(u.getAvatar());
            item.setNickName(u.getNickName());
            return item;
        }).collect(Collectors.toList()));
        return info;
    }
}
