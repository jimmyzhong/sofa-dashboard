package me.izhong.jobs.manage.impl.core.thread;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.manage.impl.core.conf.XxlJobAdminConfig;
import me.izhong.jobs.manage.impl.core.model.ZJobGroup;
import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.jobs.manage.impl.core.model.ZJobLog;
import me.izhong.jobs.type.TriggerTypeEnum;
import me.izhong.jobs.manage.impl.service.ZJobInfoService;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
import me.izhong.common.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JobFailMonitorHelper {
    private static Logger logger = LoggerFactory.getLogger(JobFailMonitorHelper.class);


    // ---------------------- monitor ----------------------
    private Thread monitorThread;
    private volatile boolean toStop = false;

    @Autowired
    private ZJobInfoService jobInfoService;

    @Autowired
    private ZJobLogService jobLogService;

    public void start() {
        monitorThread = new Thread(new Runnable() {

            @Override
            public void run() {

                // monitor
                while (!toStop) {
                    try {
                        List<Long> failLogIds = jobLogService.findFailJobLogIds();
                        if (failLogIds != null && !failLogIds.isEmpty()) {
                            for (long failLogId : failLogIds) {

                                // lock log
                                // 告警状态：0-默认、-1=锁定状态、1-无需告警、2-告警成功、3-告警失败
                                long lockRet = jobLogService.updateAlarmStatus(failLogId, 0, -1);
                                if (lockRet < 1) {
                                    continue;
                                }
                                ZJobLog log = jobLogService.selectByPId(failLogId);
                                ZJobInfo info = jobInfoService.selectByPId(log.getJobId());

                                // 1、fail retry monitor
                                if (log.getExecutorFailRetryCount() > 0) {
                                    JobTriggerPoolHelper.trigger(log.getJobId(), TriggerTypeEnum.RETRY, (log.getExecutorFailRetryCount() - 1), null);
                                    String retryMsg = "<br><br><span style=\"color:#F39C12;\" > >>>>>>>>>>>" + "重试" + "<<<<<<<<<<< </span><br>";
                                    log.setTriggerMsg(log.getTriggerMsg() + retryMsg);
                                    jobLogService.update(log);
                                }

                                // 2、fail alarm monitor
                                int newAlarmStatus = 0;
                                if (info != null && info.getAlarmEmail() != null && info.getAlarmEmail().trim().length() > 0) {
                                    boolean alarmResult = true;
                                    try {
                                        alarmResult = failAlarm(info, log);
                                    } catch (Exception e) {
                                        alarmResult = false;
                                        logger.error(e.getMessage(), e);
                                    }
                                    newAlarmStatus = alarmResult ? 2 : 3;
                                } else {
                                    newAlarmStatus = 1;
                                }
                                jobLogService.updateAlarmStatus(failLogId, -1, newAlarmStatus);
                            }
                        }

                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error("job fail monitor thread error:{}", e);
                        }
                    }
                }

                logger.info("job fail monitor thread stop");

            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("JobFailMonitorHelper");
        monitorThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }


    // ---------------------- alarm ----------------------

    // email alarm template
    private static final String mailBodyTemplate = "<h5>" + "详情" + "：</span>" +
            "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
            "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
            "      <tr>\n" +
            "         <td width=\"20%\" >" + "组" + "</td>\n" +
            "         <td width=\"10%\" >" + "ID" + "</td>\n" +
            "         <td width=\"20%\" >" + "描述" + "</td>\n" +
            "         <td width=\"40%\" >" + "内容" + "</td>\n" +
            "      </tr>\n" +
            "   </thead>\n" +
            "   <tbody>\n" +
            "      <tr>\n" +
            "         <td>{0}</td>\n" +
            "         <td>{1}</td>\n" +
            "         <td>{2}</td>\n" +
            "         <td>{3}</td>\n" +
            "      </tr>\n" +
            "   </tbody>\n" +
            "</table>";

    /**
     * fail alarm
     *
     * @param jobLog
     */
    private boolean failAlarm(ZJobInfo info, ZJobLog jobLog) {
        boolean alarmResult = true;

        // send monitor email
        if (info != null && info.getAlarmEmail() != null && info.getAlarmEmail().trim().length() > 0) {

            // alarmContent
            String alarmContent = "Alarm Job LogId=" + jobLog.getId();
            if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += "<br>TriggerMsg=<br>" + jobLog.getTriggerMsg();
            }
            if (jobLog.getHandleCode() > 0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += "<br>HandleCode=" + jobLog.getHandleMsg();
            }

            // email info
            ZJobGroup group = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupService().selectByPId(info.getJobGroupId());
            String personal = "名称";
            String title = "标题";
            String content = MessageFormat.format(mailBodyTemplate,
                    group != null ? group.getRemark() : "null",
                    info.getId(),
                    info.getJobDesc(),
                    alarmContent);

            Set<String> emailSet = new HashSet<>(Arrays.asList(info.getAlarmEmail().split(",")));
            for (String email : emailSet) {

                // make mail
//				try {
//					MimeMessage mimeMessage = XxlJobAdminConfig.getAdminConfig().getMailSender().createMimeMessage();
//
//					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//					helper.setFrom(XxlJobAdminConfig.getAdminConfig().getEmailUserName(), personal);
//					helper.setTo(email);
//					helper.setSubject(title);
//					helper.setText(content, true);
//
//					XxlJobAdminConfig.getAdminConfig().getMailSender().send(mimeMessage);
//				} catch (Exception e) {
//					logger.error(">>>>>>>>>>> xxl-job, job fail alarm email send error, JobLogId:{}", jobLog.getId(), e);
//
//					alarmResult = false;
//				}

            }
        }

        // do something, custom alarm strategy, such as sms


        return alarmResult;
    }

}
