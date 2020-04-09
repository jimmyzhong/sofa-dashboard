package me.izhong.shop.service.impl.job;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.service.impl.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceHelper {
    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void setUp() {
        ScheduledExecutorService orderStatusUpdater = Executors.newScheduledThreadPool(2,
                new ThreadFactoryBuilder()
                        .setNameFormat("order-status-updater").build());
        orderStatusUpdater.scheduleAtFixedRate(() -> {
            try {
                orderService.updateExpiredOrders();
            }catch (Throwable throwable) {
                log.error("order update expired status error", throwable);
            }
        }, 1, 2, TimeUnit.MINUTES);

        orderStatusUpdater.scheduleAtFixedRate(() -> {
            try {
                orderService.updateExpiredAuctionOrders();
            }catch (Throwable throwable) {
                log.error("order update auction expired status error", throwable);
            }
        }, 1, 2, TimeUnit.HOURS);
    }
}
