package me.izhong.shop.service.impl.job;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.JobDao;
import me.izhong.shop.entity.Job;
import me.izhong.shop.service.impl.PayRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PayRecordServiceHelper {
    @Autowired
    PayRecordService payRecordService;
    @Autowired private JobDao jobDao;

    @PostConstruct
    public void setUp() {
        ScheduledExecutorService userMoneyUpdater = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("user-money-updater").build());
        userMoneyUpdater.scheduleAtFixedRate(() -> {
            updateUserMoneyJob();
        }, 1, 3, TimeUnit.MINUTES);
    }

    private void updateUserMoneyJob() {
        Job updateUserMoneyJob = jobDao.findFirstByName("User-Money-Updater");
        if (updateUserMoneyJob == null) {
            updateUserMoneyJob = new Job();
            updateUserMoneyJob.setName("User-Money-Updater");
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.minusDays(7); // default last 7 days, unless it run success once
            // last run success
            if (updateUserMoneyJob.getLastRunState()!= null
                    && updateUserMoneyJob.getLastRunState() == 1 && updateUserMoneyJob.getLastRunTime()!=null) {
                start = updateUserMoneyJob.getLastRunTime();
            }
            LocalDateTime end = now;

            Set<Long> userIds = payRecordService.getUserIdsWhoReceivedMoneyBetween(start, end);
            userIds.addAll(userIds);

            for (Long userId: userIds) {
                payRecordService.updateUserMoney(userId, start, end);
            }
            updateUserMoneyJob.setLastRunState(1);
        }catch (Throwable throwable) {
            log.error("order update expired status error", throwable);
            updateUserMoneyJob.setLastRunState(0);
        } finally {
            updateUserMoneyJob.setLastRunTime(LocalDateTime.now());
            jobDao.save(updateUserMoneyJob);
        }
    }

}
