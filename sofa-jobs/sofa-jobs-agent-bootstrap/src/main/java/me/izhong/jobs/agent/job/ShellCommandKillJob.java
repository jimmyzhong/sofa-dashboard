package me.izhong.jobs.agent.job;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.DateUtil;
import me.izhong.jobs.agent.bean.JobContext;
import me.izhong.jobs.agent.bean.JobsConfigBean;
import me.izhong.jobs.agent.exp.JobExecutionException;
import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.agent.util.ContextUtil;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.common.model.ReturnT;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 执行script脚本的定时任务，通过run.sh调起来
 * 日志通过dubbo上送到统一调度平台
 */
@Slf4j
public class ShellCommandKillJob extends IJobHandler {

    private String SHName = "/bin/sh";
    private String command = "kill.sh";


    @Override
    public ReturnT<String> execute(JobContext jobContext) throws Exception {
        Long jobId = jobContext.getJobId();
        Long triggerId = jobContext.getTriggerId();

        JobsConfigBean configBean = ContextUtil.getBean(JobsConfigBean.class);
        JobServiceReference facade = ContextUtil.getBean(JobServiceReference.class);
        IJobMngFacade jobMng = facade.getJobMngFacade();

        String scriptDir = configBean.getScriptPath();

        log.info("shell command kill job[{}]  trigger [{}] timeout:{}s ",
                jobId, jobContext.getTriggerId(),jobContext.getTimeout());

        try {

            long timeout = jobContext.getTimeout();

            log.info("开始任务:serverName={} command={}, timeout={}", ContextUtil.getServerName(), command,timeout);

            if (StringUtils.isBlank(command)) {
                throw new JobExecutionException("参数错误");
            }

            String shellCommand = SHName + " " + scriptDir
                    + "/"+ command + " " + jobId + " " + triggerId;
            log.info("shellCommand:{}", shellCommand);

            DefaultExecutor shellExecutor = new DefaultExecutor();

            ExecuteWatchdog watchdog = new ExecuteWatchdog((timeout + 60 )* 1000 );
            shellExecutor.setWatchdog(watchdog);

            //正常退出状态码
            shellExecutor.setExitValue(0);

            File logFile = new File(configBean.getLogDir());
            if(!logFile.exists()) {
                throw new JobExecutionException("日志文件路径不存在:"+configBean.getLogDir());
            }
            String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());
            logFile = new File(configBean.getLogDir() + File.separator + dateString + File.separator + +jobId + "_" + triggerId + "_kill.txt");

            File parentDir = logFile.getParentFile();
            if(!parentDir.exists()){
                parentDir.mkdirs();
            }
            if(!logFile.exists()) {
                log.info("文件不存在 {} 创建",logFile.getAbsolutePath());
                boolean isSc = logFile.createNewFile();
                if(!isSc){
                    log.info("文件创建失败");
                }
            }

            @Cleanup
            FileOutputStream fos = new FileOutputStream(logFile);
            PumpStreamHandler streamHandler = new PumpStreamHandler(fos);
            shellExecutor.setStreamHandler(streamHandler);


            Date startTime = new Date();
            log.info("kill任务开始执行,triggerId:{} startTime:{}",triggerId,DateUtil.dateTime(startTime));

            CommandLine cmdLine = CommandLine.parse(shellCommand);
            int exitValue = shellExecutor.execute(cmdLine);
            log.info("kill任务结束了: triggerId:{} exitValue:{}",triggerId,exitValue);
            //记录执行状态信息 原来那个任务能返回，它可以自己记录
            //jobMng.uploadJobErrorStatics(triggerId, new Date(), 400, exitValue == 0 ? "执行失败（终止成功）":"终止任务异常");
            return ReturnT.SUCCESS;
        } catch (Throwable e) {
            log.error("kill任务异常，上送异常信息，triggerId:{}",triggerId, e);
            jobMng.uploadJobErrorStatics(triggerId,new Date(), 400, "kill任务执行异常:" + e.getMessage());
            return ReturnT.FAIL;
        } finally {

        }
    }

}
