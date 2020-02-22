package me.izhong.jobs.manage.impl.core.trigger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import me.izhong.common.util.IpUtil;
import me.izhong.jobs.manage.IJobAgentMngFacade;
import me.izhong.jobs.manage.impl.JobAgentServiceReference;
import me.izhong.jobs.manage.impl.core.conf.XxlJobAdminConfig;
import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.jobs.manage.impl.core.model.ZJobLog;
import me.izhong.jobs.manage.impl.core.util.SpringUtil;
import me.izhong.jobs.manage.impl.service.ZJobInfoService;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
import me.izhong.jobs.model.TriggerParam;
import me.izhong.jobs.type.ExecutorBlockStrategyEnum;
import me.izhong.jobs.type.TriggerTypeEnum;
import me.izhong.common.model.ReturnT;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JobDirectTrigger {

    private static Logger logger = LoggerFactory.getLogger(JobDirectTrigger.class);

    public static ReturnT<String> trigger(long jobId, TriggerTypeEnum triggerType,
                                          int failRetryCount, String executorParam) {
        ZJobInfoService jobInfoService = XxlJobAdminConfig.getAdminConfig().getZJobInfoService();
        ZJobLogService jobLogService = SpringUtil.getBean(ZJobLogService.class);
        JobAgentServiceReference agentServiceReference = SpringUtil.getBean(JobAgentServiceReference.class);
        ZJobInfo jobInfo = jobInfoService.selectByPId(jobId);
        if (jobInfo == null) {
            logger.warn("trigger fail, jobId invalid，jobId={}", jobId);
            return ReturnT.FAIL;
        }
        //如果是阻塞的任务，这里就不调度了
        // executor block strategy
        boolean willSchedule = false;
        ZJobLog jobLog = null;
        ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null);
        if (ExecutorBlockStrategyEnum.SERIAL_EXECUTION == blockStrategy) {
            if (jobInfo.getRunningTriggerIds() != null && jobInfo.getRunningTriggerIds().size() > 0) {
                //任务正在执行
                if (!Boolean.TRUE.equals(jobInfo.getWakeAgain())) {
                    logger.info("设置WakeAgain[{}]为TRUE RunningTriggerIds:{}", jobInfo.getJobDesc(), jobInfo.getRunningTriggerIds());
                    jobInfo.setWakeAgain(Boolean.TRUE);
                    jobInfoService.updateWaitAgain(jobInfo.getJobId(), Boolean.TRUE);
                }
                logger.info("任务[{}]正在执行中，策略[{}]放弃本次调度, RunningTriggerIds:{}", jobInfo.getJobDesc(), blockStrategy, jobInfo.getRunningTriggerIds());
                return new ReturnT<String>(ReturnT.FAIL_CODE, "放弃本次调度:" + blockStrategy.getTitle());
            }
        } else if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
            //放弃执行
            logger.info("任务[{}]正在执行中，策略[{}]放弃本次调度, RunningTriggerIds:{}", jobInfo.getJobDesc(), blockStrategy, jobInfo.getRunningTriggerIds());
            return new ReturnT<String>(ReturnT.FAIL_CODE, "放弃本次调度:" + blockStrategy.getTitle());
        } else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy){
            // 杀死之前的，调起新的
            List<ZJobLog> runningJobs = jobLogService.findRunningJobs(jobId);
            if(runningJobs != null && runningJobs.size() > 0) {
                for (ZJobLog jl : runningJobs) {
                    logger.info("任务[{}]正在执行中，策略[{}]终止这个任务, TriggerId:{}", jobInfo.getJobDesc(), blockStrategy, jl.getJobLogId());
                    agentServiceReference.jobAgentService.kill(jobId, jl.getJobLogId());
                }
                jobInfoService.updateRunningTriggers(jobId,new ArrayList<>());
            }
        } else if(ExecutorBlockStrategyEnum.CONCURRENT_EXECUTION == blockStrategy) {
            //并发执行
            Integer concurrentSize = jobInfo.getConcurrentSize();
            if (concurrentSize == null)
                concurrentSize = 2;
            if (jobInfo.getRunningTriggerIds() != null && jobInfo.getRunningTriggerIds().size() >= concurrentSize) {
                logger.info("任务[{}]正在执行中，策略[{}]并发调度数量超限[{}], RunningTriggerIds:{}", jobInfo.getJobDesc(), blockStrategy, concurrentSize, jobInfo.getRunningTriggerIds());
                return new ReturnT<String>(ReturnT.FAIL_CODE, "并发原因，放弃本次调度:" + blockStrategy.getTitle());
            }
        }

        int finalFailRetryCount = failRetryCount >= 0 ? failRetryCount : (jobInfo.getExecutorFailRetryCount() == null ? 0 : jobInfo.getExecutorFailRetryCount().intValue());

        if (StringUtils.isBlank(executorParam )) {
            executorParam = jobInfo.getExecutorParam();
        }

        jobLog = jobLogService.insertTriggerBeginMessage(jobInfo.getJobId(), jobInfo.getJobGroupId(),
                jobInfo.getJobDesc(), new Date(), triggerType.getTitle(),
                finalFailRetryCount, jobInfo.getExecutorTimeout(), executorParam, blockStrategy != null ? blockStrategy.getTitle() : null);
        jobLog.setExecutorParam(executorParam);

        jobInfo.getRunningTriggerIds().add(jobLog.getJobLogId());
        jobInfoService.updateRunningTriggers(jobLog.getJobId(), jobInfo.getRunningTriggerIds());

        // 2、init trigger-param
        TriggerParam triggerParam = new TriggerParam();
        triggerParam.setJobId(jobInfo.getJobId());
        triggerParam.setLogId(jobLog.getJobLogId());

        triggerParam.setExecutorParams(jobLog.getExecutorParam());
        triggerParam.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
        triggerParam.setExecutorTimeout(jobInfo.getExecutorTimeout());
        //=============================run
        ReturnT<String> triggerResult = runExecutor(triggerParam);

        jobLog = jobLogService.selectByPId(jobLog.getJobLogId());

        // 5、collection trigger info
        StringBuffer triggerMsgSb = new StringBuffer();
        triggerMsgSb.append("<br>触发类型：").append(triggerType.getTitle());
        triggerMsgSb.append("<br>").append("管理IP：").append(IpUtil.getHostIp());
        //triggerMsgSb.append("<br>").append("路由策略：").append(executorRouteStrategyEnum.getTitle());
        triggerMsgSb.append("<br>").append("阻塞策略：").append(blockStrategy.getTitle());
        triggerMsgSb.append("<br>").append("超时时间：").append(jobInfo.getExecutorTimeout());
        triggerMsgSb.append("<br>").append("剩余重试次数：").append(finalFailRetryCount);

        triggerMsgSb.append("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" + "触发调度" + "<<<<<<<<<<< </span><br>")
                .append(triggerResult.getMsg() != null ? triggerResult.getMsg() : "");

        // 6、save log trigger-info
        String exeP = StringUtils.isBlank(executorParam) ? jobInfo.getExecutorParam() : executorParam;
        //触发结果
        Integer triggerCode = ReturnT.SUCCESS_CODE == triggerResult.getCode() ? 0 : triggerResult.getCode();
        String triggerMsg = triggerMsgSb.toString();
        logger.info("保存jobLog jobLog.getJobLogId:{} triggerCode:{} triggerMsg:{}", jobLog.getJobLogId(), triggerCode, triggerMsg);

        jobLogService.updateTriggerDoneMessage(jobLog.getJobLogId(),
                        exeP, triggerCode, triggerMsg);

        logger.debug("job trigger end, jobId:{}", jobLog.getId());
        return triggerResult;
    }

    public static ReturnT<String> runExecutor(final TriggerParam triggerParam) {
        ReturnT<String> runResult;
        logger.info("调度远程执行器执行：{}", triggerParam);
        try {
            IJobAgentMngFacade sr = SpringUtil.getBean(JobAgentServiceReference.class).jobAgentService;

            String executorParams = triggerParam.getExecutorParams();
            Map<String, String> envs = new HashMap<String, String>();
//            if (triggerParam.getExecutorTimeout() != null) {
//                envs.put("timeout", triggerParam.getExecutorTimeout().toString());
//            }
            Map<String, String> params = new HashMap<String, String>();
            if (StringUtils.isNotBlank(executorParams)) {
                try {
                    if (executorParams.startsWith("{") && executorParams.endsWith("}")) {
                        params = JSONObject.parseObject(executorParams, new TypeReference<Map<String, String>>() {
                        });
                    }
                } catch (Exception e) {

                }
//                else if (executorParams.indexOf(",") > 0) {
//                    String[] xx = executorParams.split(",");
//                    for (String x : xx) {
//                        String[] kv = x.split("=");
//                        if (kv.length == 2)
//                            params.put(kv[0], kv[1]);
//                    }
//                }
//                else {
//                    params.put("data", executorParams);
//                }
            }
            params.put("rawParams",executorParams);
            logger.info("rpc 远程调用 jobId:{}", triggerParam.getJobId());
            //dubbo 远程调用
            Long timeout = triggerParam.getExecutorTimeout();
            runResult = sr.trigger(triggerParam.getJobId(), triggerParam.getLogId(), timeout, envs, params);
            logger.info("rpc 远程调用应答:{}", runResult);
            if (runResult == null) {
                runResult = ReturnT.FAIL;
            }
            StringBuffer runResultSB = new StringBuffer("触发调度：");
//            runResultSB.append("<br>address：").append(address);
            runResultSB.append("<br>code：").append(runResult.getCode());
            runResultSB.append("<br>msg：").append(runResult.getMsg());

            runResult.setMsg(runResultSB.toString());
            return runResult;
        } catch (Exception e) {
            logger.error("job trigger error, please check ...", e);
            return new ReturnT<String>(ReturnT.FAIL_CODE, e.toString());
        }
    }

}
