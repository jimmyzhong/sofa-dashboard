package me.izhong.jobs.agent.service;

import me.izhong.jobs.agent.bean.JobsConfigBean;
import me.izhong.jobs.agent.job.ShellCommandKillJob;
import me.izhong.jobs.agent.job.ShellCommandRunJob;
import me.izhong.jobs.agent.job.ShellCommandStatusJob;
import me.izhong.jobs.manage.IJobAgentMngFacade;
import me.izhong.jobs.agent.bean.JobContext;
import me.izhong.jobs.model.LogResult;
import me.izhong.common.model.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
//@SofaService(interfaceType = IJobAgentMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class JobAgentMngImpl implements IJobAgentMngFacade {

    @Autowired
    private JobsConfigBean jobsConfigBean;

    @Override
    public ReturnT<String> kill(Long jobId, Long triggerId) {
        log.info("kill 任务 jobId:{} ,triggerId:{}",jobId,triggerId);
        try {
            ShellCommandKillJob commandJob = new ShellCommandKillJob();
            //后面考虑缓存 进程id
            JobContext context = new JobContext(jobId, triggerId, 60, null, null);
            return commandJob.execute(context);
        } catch (Exception e) {
            log.error("", e);
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> status(Long jobId, Long triggerId) {
        log.info("status 任务 jobId:{} ,triggerId:{}",jobId,triggerId);
        try {
            ShellCommandStatusJob commandJob = new ShellCommandStatusJob();
            //后面考虑缓存 进程id
            JobContext context = new JobContext(jobId, triggerId, 60, null, null);
            return commandJob.execute(context);
        } catch (Exception e) {
            log.error("", e);
        }
        return ReturnT.SUCCESS;
    }


    @Override
    public ReturnT<String> trigger(Long jobId, Long triggerId, Long timeout, Map<String, String> envs, Map<String, String> params) {

        if (envs == null)
            envs = new HashMap();

//        默认30分钟超时
        long tt = (timeout == null ? 30 * 60 : timeout);

        JobContext context = new JobContext(jobId, triggerId, tt, envs, params);
        log.info("收到远程任务请求 jobId:{} triggerId:{}",jobId,triggerId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ShellCommandRunJob commandJob = new ShellCommandRunJob();
                    //后面考虑缓存 进程id
                    commandJob.execute(context);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }).start();
        return ReturnT.SUCCESS;
    }

    @Override
    public LogResult catLog(Long jobId, Long logId, long triggerTime,  int fromLineNum) {

        String dir = jobsConfigBean.getLogDir();
        // filePath/yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");    // avoid concurrent problem, can not be static
        File logFilePath = new File(dir, sdf.format(triggerTime));

        // filePath/yyyyMMdd/3_9999.log
        String logFileName = logFilePath.getPath()
                .concat(File.separator)
                .concat(String.valueOf(logId))
                .concat("_")
                .concat(String.valueOf(jobId))
                .concat(".txt");

        // valid log file
        if (logFileName == null || logFileName.trim().length() == 0) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not found", true);
        }
        File logFile = new File(logFileName);

        if (!logFile.exists()) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not exists", true);
        }

        // read file
        StringBuffer logContentBuffer = new StringBuffer();
        int toLineNum = 0;
        LineNumberReader reader = null;
        try {
            //reader = new LineNumberReader(new FileReader(logFile));
            String ahrEncode = Charset.defaultCharset().displayName();
            String langEncode = System.getenv("LANG");
            log.debug("ahrEncode:{} langEncode:{}",ahrEncode,langEncode);
            if(StringUtils.containsIgnoreCase(langEncode,"GBK")) {
                ahrEncode = "GBK";
            }
            //reader = new LineNumberReader(new FileReader(logFile));
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(logFile), ahrEncode));
            String line = null;

            int rCount = 0;
            while ((line = reader.readLine()) != null) {
                toLineNum = reader.getLineNumber();        // [from, to], start as 1
                if (toLineNum >= fromLineNum) {
                    rCount ++;
//                    if(rCount > 1000)
//                        break;
                    logContentBuffer.append(line).append("\n");
//                    logContentBuffer.append(toLineNum).append(" ").append(line).append("\n");
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        // result
        LogResult logResult = new LogResult(fromLineNum, toLineNum, logContentBuffer.toString(), false);
        return logResult;
    }
}
