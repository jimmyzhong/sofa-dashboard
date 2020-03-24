package me.izhong.shop.bid.test.sub;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.ShopBidApplication;
import me.izhong.shop.bid.rat.RateLimitClient;
import me.izhong.shop.bid.rat.RateLimitResult;
import me.izhong.shop.bid.rat.RateLimitVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopBidApplication.class)
@Slf4j
public class RateLimitTest2 {

    @Autowired
    private RateLimitClient rateLimitClient;

    private volatile  long s = 0;

    @Test
    public void testAcquire222() throws InterruptedException {
        //10个线程
        ExecutorService executorService = Executors.newFixedThreadPool(20);

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
            Random random = new Random();
            while (true) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean isAll = rateLimitClient.isAllowed("test2",5,5);
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
        rollingBucketStream.subscribe(summary -> {
            double timestamp = (System.currentTimeMillis() - currentMillis) / 1000.0;
            System.out.println("time:" + timestamp + ", acquired:" + summary.acquire +
                    ", reject " + summary.reject + ", error: " + summary.error);
        });
        for (int i = 0; i < 20; i++) {
            executorService.submit(acquire);
        }

        s = System.currentTimeMillis();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long end = System.currentTimeMillis();
                System.out.println("tps" + at.longValue() * 1.0 / 2  ) ;
                s = System.currentTimeMillis();
                at.set(0);
            }
        },0,2000);

        while (true) {
            Thread.sleep(5000);
        }
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
