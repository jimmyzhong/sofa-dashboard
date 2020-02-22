package me.izhong.jobs.manage.impl.core.thread;

import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.service.MongoDistributedLock;
import me.izhong.jobs.manage.impl.JobAgentServiceReference;
import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.jobs.manage.impl.core.model.ZJobLog;
import me.izhong.jobs.manage.impl.service.ZJobInfoService;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
import me.izhong.common.model.ReturnT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JobStateMonitorHelper {

    // ---------------------- monitor ----------------------
    private Thread monitorThread;
    private volatile boolean toStop = false;
    public static final String LOCK_KEY = "job_state_monitor";

    @Autowired
    private ZJobInfoService jobInfoService;

    @Autowired
    private ZJobLogService jobLogService;

    @Autowired
    private JobAgentServiceReference jobAgentServiceReference;

    @Autowired
    private MongoDistributedLock dLock;

    @PostConstruct
    public void start() {
        monitorThread = new Thread(new Runnable() {

            @Override
            public void run() {

                while (!toStop) {
                    boolean lock = false;
                    try {
                        lock = dLock.getLock(LOCK_KEY, 8000);
                        List<ZJobLog> jobLogs = jobLogService.findRunningJobs();
                        if (jobLogs != null && !jobLogs.isEmpty()) {
                            for (ZJobLog jobLog : jobLogs) {
                                if (jobLog.getTriggerCode() == null) {
                                    //20s 触发失败的任务 直接关闭
                                    if(System.currentTimeMillis() - jobLog.getTriggerTime().getTime() > 20 * 1000) {
                                        log.info("异常结束，超时未执行 TriggerCode:{}，(stats触发)",jobLog.getTriggerCode());
                                        setJobLogResult(jobLog, 404, "异常结束，超时20s未执行(stats触发)");
                                        continue;
                                    }
                                } else if (jobLog.getTriggerCode().intValue() !=0) {
                                    log.info("异常结束 触发失败 TriggerCode:{}，(stats触发)",jobLog.getTriggerCode());
                                    setJobLogResult(jobLog, 404, "异常结束，触发失败(stats触发)");
                                    continue;
                                }
                                //
                                if(jobLog.getExecutorTimeout() != null && jobLog.getExecutorTimeout().longValue()  > 0) {
                                    //结束超时的任务
                                    Date triggerTime = jobLog.getTriggerTime();
                                    if(triggerTime != null && (System.currentTimeMillis() - triggerTime.getTime())/1000 > jobLog.getExecutorTimeout().longValue()){
                                        ReturnT<String> rt = jobAgentServiceReference.jobAgentService.kill(jobLog.getJobId(),jobLog.getJobLogId());
                                        log.info("任务{}:{}超时了发出kill命令,结果{}",jobLog.getJobId(),jobLog.getJobLogId(),rt);
                                        //continue; 去掉，否则下面的状态检测无效了
                                    }
                                }
                                //去检测任务是不是还活着
                                try {
                                    ReturnT<String> rtStatus = jobAgentServiceReference.jobAgentService.status(jobLog.getJobId(), jobLog.getJobLogId());
                                    if (rtStatus.getCode() == ReturnT.SUCCESS_CODE) {
                                        int code = rtStatus.getCode();
                                        String content = rtStatus.getContent();
                                        String message = rtStatus.getMsg();
                                        log.info("检测任务,收到任务运行状态, jobId:{}jobDesc:{} jobLogId:{} code:{} content:{} message:{}", jobLog.getJobId(), jobLog.getJobDesc(), jobLog.getJobLogId(), code, content, message);

                                        //任务已经结束
                                        Date oneMinutesAgo = DateUtils.addMinutes(new Date(),-1);
                                        Date cTime = jobLog.getCreateTime();
                                        if (StringUtils.equals(content, "DONE")) {
                                            if(cTime == null || cTime.before(oneMinutesAgo))
                                                setJobLogResult(jobLog, 404, "异常结束,进程没找到(stats触发)");
                                        }
                                    }
                                } catch (Exception jobLogException) {
                                    log.error("status检测异常",jobLogException);
                                    if (jobLog.getTriggerTime() != null) {
                                        Date triggerTimeDes = DateUtils.addMinutes(jobLog.getTriggerTime(), 1);
                                        //已经过去一分钟
                                        if (triggerTimeDes.before(new Date())) {
                                            //检测报错了，过去了一分钟，还没吊起任务，直接失败
                                            if (jobLog.getHandleCode() == null) {
                                                setJobLogResult(jobLog, 404, "异常结束，任务检测异常, 60s未执行(stats触发)");
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        List<ZJobInfo> jobInfos = jobInfoService.findRunningJobs();
                        for(ZJobInfo jobInfo : jobInfos ){
                            List<Long> triggerIds = jobInfo.getRunningTriggerIds();
                            boolean needUpdate = false;
                            if(triggerIds != null && triggerIds.size() > 0) {
                                Iterator<Long> it = triggerIds.iterator();
                                while (it.hasNext()) {
                                    Long tgid = it.next();
                                    ZJobLog jl = jobLogService.selectByPId(tgid);
                                    if(jl == null || jl.getHandleCode() != null) {
                                        //已经结束
                                        it.remove();
                                        needUpdate = true;
                                    }
                                }
                            }
                            if(needUpdate) {
                                log.info("更新任务{}正在运行的trigger:{}",jobInfo.getJobDesc(),triggerIds);
                                jobInfoService.updateRunningTriggers(jobInfo.getJobId(),triggerIds);
                            }
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.error("任务监控线程异常", e);
                        }
                    } finally {
                        if(lock)
                            dLock.releaseLock(LOCK_KEY);
                    }
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                log.info("job fail monitor thread stop");
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("JobStateMonitorHelper");
        monitorThread.start();
    }

    @PreDestroy
    public void toStop() {
        toStop = true;
        // interrupt and wait
        monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void setJobLogResult(ZJobLog jobLog, Integer code, String message){
        jobLogService.updateHandleDoneMessage(jobLog.getJobLogId(), code, message);
        ZJobInfo jobInfo = jobInfoService.selectByPId(jobLog.getJobId());
        if (jobInfo.getRunningTriggerIds() != null && jobInfo.getRunningTriggerIds().contains(jobLog.getJobLogId())) {
            jobInfo.getRunningTriggerIds().remove(jobLog.getJobLogId());
            log.info("移除已经停止任务{},当前还有任务{}", jobLog.getJobLogId(), jobInfo.getRunningTriggerIds());
            jobInfoService.updateRunningTriggers(jobInfo.getJobId(), jobInfo.getRunningTriggerIds());
        } else {
            log.info("当前运行 {} 队列不包含异常任务（任务进行中）:triggerId:{}", jobInfo.getJobDesc(), jobLog.getJobLogId());
        }
    }

}
