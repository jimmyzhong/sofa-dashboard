package me.izhong.shop.bid.action;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.ann.ActionNode;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.IFilterCallback;
import me.izhong.shop.bid.ntt.NttTaskExecutor;
import me.izhong.shop.bid.rat.RateLimitResult;
import me.izhong.shop.bid.service.RateLimitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@ActionNode(name = "测试TPS服务", url = "/test/tps")
public class TestTpsAction implements IActionNode {

    @Autowired
    private NttTaskExecutor nttTaskExecutor;

    @Autowired
    private RateLimitService rateLimitService;

    private volatile long s = 0;

    private volatile boolean start = true;

    private  Subscription sub ;

    ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(20);

    @Override
    public void process(BidContext context, IFilterCallback callback) throws BusinessException {
        log.info("start service");
        JSONObject json = context.getJsonObjectRequest();
        String flag = json.getString("flag");
        if(StringUtils.equals(flag,"stop")) {
            start = false;
            threadPoolExecutor.shutdownNow();
            if(sub != null)
                sub.unsubscribe();
            callback.onPostProcess(context);
            return;
        }
        if(!StringUtils.equals(flag,"start")) {
            callback.onPostProcess(context);
            return;
        }

        int tps = json.getIntValue("tps");
        int cap = json.getIntValue("cap");

        Subject<RateLimitSummary, RateLimitSummary> writeSubject = new SerializedSubject<RateLimitSummary, RateLimitSummary>(PublishSubject.<RateLimitSummary>create());
        Observable<RateLimitSummary> readSubject = writeSubject.share();
        Observable<RateLimitSummary> bucketStream = Observable.defer(() -> {
            return readSubject.window(200, TimeUnit.MILLISECONDS)
                    .flatMap(
                            observable ->
                                    observable.reduce(new RateLimitSummary(0, 0, 0),
                                            (a, b) -> a.reduce(b))
                    );
        });
        Observable<RateLimitSummary> rollingBucketStream = bucketStream.window(5, 1)
                .flatMap(observable -> observable.reduce(new RateLimitSummary(0, 0, 0),
                        (a, b) -> a.reduce(b)));

        AtomicInteger at = new AtomicInteger(0);
        Runnable acquire = () -> {
            while (start) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean isAll = rateLimitService.acquireBid("test2");
                if(isAll) {
                    at.incrementAndGet();
                    writeSubject.onNext(new RateLimitSummary(RateLimitResult.getResult(1L)));
                } else {
                    writeSubject.onNext(new RateLimitSummary(RateLimitResult.getResult(-1L)));
                }
            }
        };
        //初始时间
        final long currentMillis = System.currentTimeMillis();
        sub = rollingBucketStream.subscribe(summary -> {
            double timestamp = (System.currentTimeMillis() - currentMillis) / 1000.0;
            log.info("time:" + timestamp + ", acquired:" + summary.acquire +
                    ", reject " + summary.reject + ", error: " + summary.error);
        });

        for (int i = 0; i < 20; i++) {
            threadPoolExecutor.submit(acquire);
        }

        s = System.currentTimeMillis();
        nttTaskExecutor.getScheduledExecutor().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(start) {
                    long end = System.currentTimeMillis();
                    log.info("tps" + at.longValue() * 1.0 / 2);
                    s = System.currentTimeMillis();
                    at.set(0);
                }
            }
        },0,2, TimeUnit.SECONDS);

        callback.onPostProcess(context);
    }

    private static class RateLimitSummary {
        public int acquire;
        public int reject;
        public int error;

        public RateLimitSummary(RateLimitResult result) {
            this.acquire = result == RateLimitResult.SUCCESS ? 1 : 0;
            this.reject = result == RateLimitResult.ACQUIRE_FAIL ? 1 : 0;
            this.error = result == RateLimitResult.ERROR ? 1 : 0;
        }

        public RateLimitSummary(int acquire, int reject, int error) {
            this.acquire = acquire;
            this.reject = reject;
            this.error = error;
        }

        public RateLimitSummary reduce(RateLimitSummary toAdd) {
            return new RateLimitSummary(this.acquire + toAdd.acquire,
                    this.reject + toAdd.reject,
                    this.error + toAdd.error);

        }
    }
}
