package me.izhong.shop.bid.test.sub;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.ShopBidApplication;
import me.izhong.shop.bid.rat.RateLimitClient;
import me.izhong.shop.bid.rat.RateLimitResult;
import me.izhong.shop.bid.rat.RateLimitVo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisPoolConfig;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopBidApplication.class)
@Slf4j
public class RateLimitTest {

    @Autowired
    private RateLimitClient rateLimitClient;

    @Test
    public void testInit(){
        RateLimitVo vo = new RateLimitVo();
        vo.setInitialPermits(10);
        vo.setMaxPermits(10);
        vo.setInterval(1.0);
        rateLimitClient.delete("test");
        rateLimitClient.init("test", vo);
        log.info("rateLimitClient初始化结束");
    }

    @Test
    public void testDelete(){
        rateLimitClient.delete("test");
        log.info("rateLimitClient delete");
    }


    @Test
    public void testAcquire() throws InterruptedException {
        //10个线程
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        Subject<RateLimitSummary, RateLimitSummary> writeSubject = new SerializedSubject<RateLimitSummary, RateLimitSummary>(PublishSubject.<RateLimitSummary>create());
        Observable<RateLimitSummary> readSubject = writeSubject.share();
        Observable<RateLimitSummary> bucketStream = Observable.defer(()->{
            return readSubject.window(200, TimeUnit.MILLISECONDS)
                    .flatMap(
                            observable->
                                    observable.reduce(new RateLimitSummary(0,0,0),
                                            (a, b)-> a.reduce(b))
                    );
        });
        Observable<RateLimitSummary> rollingBucketStream = bucketStream.window(5, 1)
                .flatMap(observable->observable.reduce(new RateLimitSummary(0, 0, 0),
                        (a, b)-> a.reduce(b)));

        Runnable acquire = () -> {
            Random random = new Random();
            while(true){
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RateLimitResult result = rateLimitClient.acquire("test4");
                writeSubject.onNext(new RateLimitSummary(result));
            }
        };
        //初始时间
        final long currentMillis = System.currentTimeMillis();
        rollingBucketStream.subscribe(summary->{
            double timestamp = (System.currentTimeMillis() - currentMillis)/1000.0;
            System.out.println("time:"+ timestamp + ", acquired:" + summary.acquire +
                    ", reject " + summary.reject + ", error: " + summary.error);
        });
        for(int i=0;i<20;i++){
            executorService.submit(acquire);
        }
        while(true){
            Thread.sleep(5000);
        }
    }

    private static class RateLimitSummary{
        public int acquire;
        public int reject;
        public int error;

        public RateLimitSummary(RateLimitResult result){
            this.acquire = result == RateLimitResult.SUCCESS?1:0;
            this.reject = result == RateLimitResult.ACQUIRE_FAIL?1:0;
            this.error = result == RateLimitResult.ERROR?1:0;
        }

        public RateLimitSummary(int acquire, int reject, int error){
            this.acquire = acquire;
            this.reject = reject;
            this.error = error;
        }

        public RateLimitSummary reduce(RateLimitSummary toAdd){
            return new RateLimitSummary(this.acquire + toAdd.acquire,
                    this.reject + toAdd.reject,
                    this.error + toAdd.error);

        }
    }

}