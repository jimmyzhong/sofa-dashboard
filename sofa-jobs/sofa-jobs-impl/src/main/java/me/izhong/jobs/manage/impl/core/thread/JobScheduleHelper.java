package me.izhong.jobs.manage.impl.core.thread;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.DateUtil;
import me.izhong.db.mongo.service.MongoDistributedLock;
import me.izhong.jobs.manage.impl.core.cron.CronExpression;
import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.jobs.type.TriggerTypeEnum;
import me.izhong.jobs.manage.impl.service.ZJobInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JobScheduleHelper {
    private static Logger logger = LoggerFactory.getLogger(JobScheduleHelper.class);

    public static final long PRE_READ_MS = 5000;    // pre read
    public static final String LOCK_KEY = "job_scheduler";

    private Thread scheduleThread;
    private Thread ringThread;
    private volatile boolean scheduleThreadToStop = false;
    private volatile boolean ringThreadToStop = false;
    private volatile static Map<Long, List<Long>> ringData = new ConcurrentHashMap<>();

    @Resource
    private ZJobInfoService zJobInfoService;

    @Autowired
    private MongoDistributedLock dLock;

    @PostConstruct
    public void start(){

        // schedule thread
        scheduleThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis()%1000 );
                } catch (InterruptedException e) {
                    if (!scheduleThreadToStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
                logger.info("初始化调度管理器成功");

                while (!scheduleThreadToStop) {

                    // Scan Job
                    long start = System.currentTimeMillis();

                    boolean preReadSuc = true;
                    boolean lock = false;
                    try {

                        lock = dLock.getLock(LOCK_KEY, 8000);

                        if(!lock){
                            log.info("没有获取到锁:{}",LOCK_KEY);
                            TimeUnit.MILLISECONDS.sleep(500);
                            continue;
                        }
                        //TimeUnit.SECONDS.sleep(20);

                        // 1、pre read
                        long nowTime = System.currentTimeMillis();
                        log.info("去数据库查询最近要执行的任务列表:{}",DateUtil.parseDateToStr(DateUtil.YYYY_MM_DD_HH_MM_SS, new Date(nowTime + PRE_READ_MS)));
                        List<ZJobInfo> scheduleList = zJobInfoService.scheduleJobQuery(nowTime + PRE_READ_MS);
                        if(scheduleList == null || scheduleList.size() == 0) {
                            //logger.info("没有轮询到要执行的定时任务");
                            preReadSuc = false;
                        } else {
                            logger.info("获取定时任务数量 {}", scheduleList.size());

                            // 2、push time-ring
                            for (ZJobInfo jobInfo: scheduleList) {

                                // time-ring jump 好久没运行的定时任务，+5s 也没到达当前时间，设置运行时间等待下次调度
                                if (nowTime > jobInfo.getTriggerNextTime() + PRE_READ_MS) {
                                    // 2.1、trigger-expire > 5s：pass && make next-trigger-time

                                    // fresh next
                                    Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date());
                                    if (nextValidTime != null) {
                                        jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
                                        log.info("设置任务 [{}:{}] 下次调度时间 {} -> {} ",jobInfo.getJobId(), jobInfo.getJobDesc(),
                                                DateUtil.parseDateToStr(DateUtil.YYYY_MM_DD_HH_MM_SS, new Date(jobInfo.getTriggerNextTime())),
                                                DateUtil.parseDateToStr(DateUtil.YYYY_MM_DD_HH_MM_SS, nextValidTime));
                                        jobInfo.setTriggerNextTime(nextValidTime.getTime());
                                    } else {
                                        jobInfo.setTriggerStatus(0L);
                                        jobInfo.setTriggerLastTime(0L);
                                        jobInfo.setTriggerNextTime(0L);
                                    }

                                } else if (nowTime > jobInfo.getTriggerNextTime()) {
                                    // 2.2、trigger-expire < 5s：direct-trigger && make next-trigger-time
                                    //任务在刚刚过去的5s以内 马上运行
                                    CronExpression cronExpression = new CronExpression(jobInfo.getJobCron());
                                    long nextTime = cronExpression.getNextValidTimeAfter(new Date()).getTime();

                                    // 1、trigger
                                    JobTriggerPoolHelper.trigger(jobInfo.getJobId(), TriggerTypeEnum.CRON, -1,  null);
                                    logger.debug("触发任务执行 : jobId = " + jobInfo.getId() );

                                    // 2、fresh next
                                    jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
                                    jobInfo.setTriggerNextTime(nextTime);


                                    // next-trigger-time in 5s, pre-read again
                                    if (jobInfo.getTriggerNextTime() - nowTime < PRE_READ_MS) {

                                        // 1、make ring second
                                        int ringSecond = (int)((jobInfo.getTriggerNextTime()/1000)%60);

                                        // 2、push time ring
                                        pushTimeRing(ringSecond, jobInfo.getJobId());

                                        // 3、fresh next
                                        Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date(jobInfo.getTriggerNextTime()));
                                        if (nextValidTime != null) {
                                            jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
                                            log.info("B设置任务 [{}:{}] 下次调度时间 {} -> {} ",jobInfo.getJobId(), jobInfo.getJobDesc(),
                                                    DateUtil.parseDateToStr(DateUtil.YYYY_MM_DD_HH_MM_SS, new Date(jobInfo.getTriggerNextTime())),
                                                    DateUtil.parseDateToStr(DateUtil.YYYY_MM_DD_HH_MM_SS, nextValidTime));
                                            jobInfo.setTriggerNextTime(nextValidTime.getTime());
                                        } else {
                                            jobInfo.setTriggerStatus(0L);
                                            jobInfo.setTriggerLastTime(0L);
                                            jobInfo.setTriggerNextTime(0L);
                                        }
                                    }
                                } else {
                                    // 2.3、trigger-pre-read：time-ring trigger && make next-trigger-time
                                    // 未来5s内要运行的，设置到准备运行
                                    // 1、make ring second
                                    int ringSecond = (int)((jobInfo.getTriggerNextTime()/1000)%60);

                                    // 2、push time ring
                                    pushTimeRing(ringSecond, jobInfo.getJobId());

                                    // 3、fresh next
                                    Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date(jobInfo.getTriggerNextTime()));
                                    if (nextValidTime != null) {
                                        jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
                                        jobInfo.setTriggerNextTime(nextValidTime.getTime());
                                    } else {
                                        jobInfo.setTriggerStatus(0L);
                                        jobInfo.setTriggerLastTime(0L);
                                        jobInfo.setTriggerNextTime(0L);
                                    }

                                }
                                zJobInfoService.scheduleUpdate(jobInfo);
                            }
                        }
                    } catch (Exception e) {
                        if (!scheduleThreadToStop) {
                            logger.error("调度异常:{}", e);
                        }
                    } finally {
                        if(lock)
                            dLock.releaseLock(LOCK_KEY);
                    }
                    long cost = System.currentTimeMillis()-start;

                    // Wait seconds, align second
                    if (cost < 1000) {  // scan-overtime, not wait
                        try {
                            // pre-read period: success > scan each second; fail > skip this period;
                            TimeUnit.MILLISECONDS.sleep((preReadSuc?1000:PRE_READ_MS) - System.currentTimeMillis()%1000);
                        } catch (InterruptedException e) {
                            if (!scheduleThreadToStop) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }

                }

                logger.info("调度器停止");
            }
        });
        scheduleThread.setDaemon(true);
        scheduleThread.setName("job-scheduleThread");
        scheduleThread.start();


        // ring thread
        ringThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // align second
                try {
                    TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis()%1000 );
                } catch (InterruptedException e) {
                    if (!ringThreadToStop) {
                        logger.error(e.getMessage(), e);
                    }
                }

                while (!ringThreadToStop) {

                    try {
                        // second data
                        List<Long> ringItemData = new ArrayList<>();
                        int nowSecond = Calendar.getInstance().get(Calendar.SECOND);   // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                        for (int i = 0; i < 2; i++) {
                            Long ll = Long.valueOf( (nowSecond+60-i)%60 );
                            List<Long> tmpData = ringData.remove(ll);
                            if (tmpData != null) {
                                ringItemData.addAll(tmpData);
                            }
                        }

                        // ring trigger
                        logger.debug(">>>>>>>>>>> time-ring beat : " + nowSecond + " = " + Arrays.asList(ringItemData) );
                        if (ringItemData!=null && ringItemData.size()>0) {
                            // do trigger
                            for (long jobId: ringItemData) {
                                // do trigger
                                log.info("time-ring 触发任务:[{}]",jobId);
                                JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.CRON, -1,  null);
                            }
                            // clear
                            ringItemData.clear();
                        }
                    } catch (Exception e) {
                        if (!ringThreadToStop) {
                            logger.error("JobScheduleHelper#ringThread error:{}", e);
                        }
                    }

                    // next second, align second
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis()%1000);
                    } catch (InterruptedException e) {
                        if (!ringThreadToStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                logger.info("JobScheduleHelper#ringThread stop");
            }
        });
        ringThread.setDaemon(true);
        ringThread.setName("job-ringThread");
        ringThread.start();
    }

    private void pushTimeRing(long ringSecond, Long jobId){
        // push async ring
        List<Long> ringItemData = ringData.get(ringSecond);
        if (ringItemData == null) {
            ringItemData = new ArrayList();
            ringData.put(ringSecond, ringItemData);
        }
        ringItemData.add(jobId);

        logger.info("调度增加到 time-ring : " + ringSecond + " = " + Arrays.asList(ringItemData) );
    }

    @PreDestroy
    public void toStop(){

        // 1、stop schedule
        scheduleThreadToStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);  // wait
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        if (scheduleThread.getState() != Thread.State.TERMINATED){
            // interrupt and wait
            scheduleThread.interrupt();
            try {
                scheduleThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // if has ring data
        boolean hasRingData = false;
        if (!ringData.isEmpty()) {
            for (Long second : ringData.keySet()) {
                List<Long> tmpData = ringData.get(second);
                if (tmpData!=null && tmpData.size()>0) {
                    hasRingData = true;
                    break;
                }
            }
        }
        if (hasRingData) {
            try {
                TimeUnit.SECONDS.sleep(8);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // stop ring (wait job-in-memory stop)
        ringThreadToStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        if (ringThread.getState() != Thread.State.TERMINATED){
            // interrupt and wait
            ringThread.interrupt();
            try {
                ringThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.info("JobScheduleHelper stop");
    }

}
