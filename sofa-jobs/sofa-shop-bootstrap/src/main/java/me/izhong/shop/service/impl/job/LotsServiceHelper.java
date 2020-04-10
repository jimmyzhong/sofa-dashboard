package me.izhong.shop.service.impl.job;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.DateUtil;
import me.izhong.common.util.IpUtil;
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
import java.util.Arrays;
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
        //8个线程，防止某个卡住
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8,
                new ThreadFactoryBuilder()
                        .setNameFormat(JOB_NAME).build());


        //15s就执行一次
        scheduler.scheduleAtFixedRate(() -> {
            try {
                subscribeBids(scheduler);
            }catch (Exception e){
                log.error("schedule error",e);
            }
        }, 1, 15, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                processEndedLots();
            }catch (Exception e){
                log.error("process ended lots error", e);
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    private void processEndedLots() {
        List<Lots> list = lotsDao.findAllByEndTimeBeforeAndFollowCountGreaterThanAndPayStatusIsNull(
                LocalDateTime.now().minusMinutes(5),  0);

        for (Lots lot: list) {
            try {
                endBid(lot.getLotsNo());
            }catch (Exception e) {
                log.error("end bid error", e);
            }
        }
    }

    private void subscribeBids(ScheduledExecutorService scheduler) {
        LocalDateTime expect = LocalDateTime.now();
//        if (!jobService.acquireJob(JOB_NAME, expect, expect.plusSeconds(15))) {
//            log.warn("unable to acquire job " + JOB_NAME);
//            return;
//        }

        LocalDateTime now = LocalDateTime.now();
        //1分钟将要开始的上架
        List<Lots> lotsList = lotsDao.findAllByStartTimeBetweenAndUploadedOrderByStartTime(now.minusMinutes(2),now.plusSeconds(60), 0);
        log.info("prepare to upload bids size {}", lotsList == null ? 0 : lotsList.size());
        for (Lots lots : lotsList) {
            try {
                BidUploadInfo bid = convert2Bid(lots);
                log.info("begin upload bid {} .", bid.getBidId());
                Boolean resu = bidActionFacade.uploadBid(bid);
                if(resu != null && resu.booleanValue()) {
                    //上架成功
                    log.info("upload bid {} success.", bid.getBidId());
                    schedulerBidEnd(scheduler, lots.getEndTime(), bid);
                    service.markLotsAsUploadedSuccess(lots,"上架成功"+ IpUtil.getHostName());
                } else {
                    //上架失败
                    log.info("upload bid {} fail.", bid.getBidId());
                    service.markLotsAsUploadedFail(lots,"上架失败");
                }
            } catch (Exception e) {
                //上架失败
                log.error("schedule bid error.", e);
                service.markLotsAsUploadedFail(lots,"上架失败：" + e.getMessage());
            }
        }
    }

    private void schedulerBidEnd(ScheduledExecutorService scheduler, LocalDateTime endTime, BidUploadInfo bid) {
        Long seconds = Duration.between(LocalDateTime.now(), endTime).getSeconds() + 5 * 60;
        scheduler.schedule(()->{
            endBid(bid.getBidId());
        }, seconds, TimeUnit.SECONDS);
        log.info("subscribe bid end at {} seconds later", seconds.toString());
    }

    public void endBid(String bidId) {
        BidDownloadInfo info = null;
        try{
            info = bidActionFacade.downloadBid(bidId, 1L, null);
        }catch (Exception e) {
            log.error("download bid error " + bidId, e);
        }

        if (info!= null && !info.getIsOver()) {
            log.warn("拍卖没有结束" + bidId);
        }
        service.saveLots(bidId, info);
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
        info.setReservePrice(lots.getReservePrice().multiply(BigDecimal.valueOf(100)).longValue());
        info.setPercentAmount(Long.valueOf(LotsService.AUCTION_PER_RETURN_PERCENTAGE));
        info.setPercentPoint(Long.valueOf(LotsService.AUCTION_PER_RETURN_SCORE_PERCENTAGE));

        List<User> userList = userDao.selectAcutionUsers(MoneyTypeEnum.AUCTION_MARGIN.getType(), lots.getId(),
                OrderStateEnum.PAID.getState());
        info.setUsers(userList.stream().map(u->{
            UserItem item = getUserItem(u);
            return item;
        }).collect(Collectors.toList()));
        return info;
    }

    private UserItem getUserItem(User u) {
        UserItem item = new UserItem();
        item.setUserId(u.getId());
        item.setAvatar(u.getAvatar());
        item.setNickName(u.getNickName());
        return item;
    }

    public void addUser(String lotsNo, Long userId) {
        User user = userDao.findById(userId).get();
        if (user == null) {
            log.warn("user does not exist." + userId);
            return;
        }
        UserItem item = getUserItem(user);
        bidActionFacade.addBidUsers(lotsNo, Arrays.asList(item));
    }
}
