package me.izhong.shop.bid.ntt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.config.ConfigBean;
import me.izhong.shop.bid.config.ErrCode;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.ITask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Component
@Slf4j
public class NttTaskExecutor {

    @Autowired
    private ConfigBean configBean;

    @Getter
    private ScheduledExecutorService scheduledExecutor;

    @Getter
    private ExecutorService synchronousExecutor;

    @PostConstruct
    public void start() {
        scheduledExecutor = new ScheduledThreadPoolExecutor(configBean.getScheduledTaskExecutorPoolSize());

        int min = configBean.getMinTaskExecutorPoolSize();
        int max = configBean.getMaxTaskExecutorPoolSize();
        synchronousExecutor = new ThreadPoolExecutor(min, max, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        log.info("ntt线程已经启动");
    }

    @PreDestroy
    public void stop() {
        scheduledExecutor.shutdown();
        synchronousExecutor.shutdown();
    }

    public void executeImmediately(final BidContext context, final ITask task) {
        try {
            synchronousExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        if (context != null
                                && context.getExceptionHandler() != null) {
                            context.getExceptionHandler().handleException(context, e);
                        } else {
                            log.error("未捕获异常：", e);
                        }
                    }
                }
            });
        } catch (RejectedExecutionException re) {
            log.error("超载，线程池耗尽！");
            if (context != null && context.getExceptionHandler() != null) {
                BusinessException be = new BusinessException(400, "繁忙，请稍候再试。");
                context.getExceptionHandler().handleException(context, be);
            }
        }
    }

    public void execute(final BidContext context, final ITask task, int delay) {
        try {
            scheduledExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        if (context != null
                                && context.getExceptionHandler() != null) {
                            context.getExceptionHandler().handleException(context,
                                    e);
                        } else {
                            log.error("未捕获异常：", e);
                        }
                    }
                }
            }, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException re) {
            log.error("超载，线程池耗尽！");
            if (context != null && context.getExceptionHandler() != null) {
                BusinessException be = new BusinessException(ErrCode.SYS_ERR, "繁忙，请稍候再试。");
                context.getExceptionHandler().handleException(context, be);
            }
        }
    }

    public void execute(final BidContext context, final ITask task) {
        try {
            scheduledExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        if (context != null
                                && context.getExceptionHandler() != null) {
                            context.getExceptionHandler().handleException(context,
                                    e);
                        } else {
                            log.error("未捕获异常：", e);
                        }
                    }
                }
            });
        } catch (RejectedExecutionException re) {
            log.error("超载，线程池耗尽！");
            if (context != null && context.getExceptionHandler() != null) {
                BusinessException be = new BusinessException(ErrCode.SYS_ERR, "请稍候再试。");
                context.getExceptionHandler().handleException(context, be);
            }
        }
    }

}
