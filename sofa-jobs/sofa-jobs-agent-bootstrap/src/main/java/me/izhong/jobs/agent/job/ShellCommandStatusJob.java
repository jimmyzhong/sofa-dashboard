package me.izhong.jobs.agent.job;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.agent.bean.JobContext;
import me.izhong.jobs.agent.bean.JobsConfigBean;
import me.izhong.jobs.agent.util.ContextUtil;
import me.izhong.common.model.ReturnT;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

/**
 * 执行script脚本的定时任务，通过run.sh调起来
 * 日志通过dubbo上送到统一调度平台
 */
@Slf4j
public class ShellCommandStatusJob extends IJobHandler {

    private String SHName = "/bin/sh";
    private String command = "status.sh";

    @Override
    public ReturnT<String> execute(JobContext jobContext) throws Exception {
        Long jobId = jobContext.getJobId();
        Long triggerId = jobContext.getTriggerId();

        JobsConfigBean configBean = ContextUtil.getBean(JobsConfigBean.class);

        String scriptDir = configBean.getScriptPath();

        log.info("shell command status job[{}]  trigger [{}] timeout:{} ",
                jobId, jobContext.getTriggerId(),jobContext.getTimeout());

        try {

            long timeout = jobContext.getTimeout();

            log.info("开始任务:serverName={} command={}, timeout={}", ContextUtil.getServerName(), command,timeout);

            String shellCommand = SHName + " " + scriptDir
                    + "/"+ command + " " + jobId + " " + triggerId;
            log.info("shellCommand:{}", shellCommand);

            DefaultExecutor shellExecutor = new DefaultExecutor();
            CommandLine cmdLine = CommandLine.parse(shellCommand);
            int exitValue = shellExecutor.execute(cmdLine);
            log.info("status任务结束了: triggerId:{} exitValue:{}",triggerId,exitValue);
            //记录执行状态信息
            if(exitValue == 2) {
                return ReturnT.successReturn("DONE");
            } else if(exitValue == 1){
                log.info("异常，查询到多个进程，正常只有一个");
            }
            return ReturnT.SUCCESS;
        } catch (ExecuteException e) {
            log.error("",e);
            int exitValue = e.getExitValue();
            log.info("status任务异常结束了: triggerId:{} exitValue:{}",triggerId,exitValue);
            if(exitValue == 2) {
                return ReturnT.successReturn("DONE");
            }
            return ReturnT.SUCCESS;
        }  catch (Throwable e) {
            log.error("status任务失败 triggerId:{}",triggerId, e);
            return ReturnT.FAIL;
        } finally {

        }
    }

}
