package me.izhong.shop.interceptor;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Aspect
@Component
public class OptimisticLockRetryAspect {
    @Value("${order.submit.retry.maxtimes}")
    private Integer submitRetryTimes;

    @Pointcut("@annotation(me.izhong.shop.annotation.NeedOptimisticLockRetry)") //self-defined pointcount for RetryOnFailure
    public void retryOnFailure(){}

    @Around("retryOnFailure()") //around can be execute before and after the point
    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
        int attempts = 0;
        do {
            attempts++;
            try {
                return pjp.proceed();
            } catch (Exception e) {
                if(e instanceof ObjectOptimisticLockingFailureException ||
                        e instanceof StaleObjectStateException) {
                    log.info("retrying....times:{}", attempts);
                    if(attempts > submitRetryTimes) {
                        log.info("retry excceed the max times..");
                        throw BusinessException.build("当前服务较忙,请稍后重试");
                    }
                }

            }
        } while (attempts < submitRetryTimes);
        return  null;
    }
}
