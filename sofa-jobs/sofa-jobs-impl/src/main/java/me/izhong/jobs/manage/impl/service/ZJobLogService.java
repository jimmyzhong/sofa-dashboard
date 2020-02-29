package me.izhong.jobs.manage.impl.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.jobs.manage.impl.core.model.ZJobLog;

import java.util.Date;
import java.util.List;

public interface ZJobLogService extends CrudBaseService<Long, ZJobLog> {
    long triggerCountByHandleCode(int successCode);

    List<Long> findFailJobLogIds();

    List<ZJobLog> findRunningJobs();

    List<ZJobLog> findRunningJobs(Long jobId);

    List<ZJobLog> findJobLogByJobId(Long jobId);

    ZJobLog insertTriggerBeginMessage(Long jobId, Long jobGroupId, String jobDesc, Date trggerTime, String triggerType,
                                      Integer finalFailRetryCount, Long executorTimeout, String executorParam, String blockStrategy);

    void updateTriggerDoneMessage(Long jobLogId, String executorParam,
                                  Integer triggerCode, String triggerMsg);

    void updateProcessRemark(Long jobLogId, Long processResult, String processMessage);

    void updateHandleStartMessage(Long jobLogId, Date startTime);

    void updateHandleDoneMessage(Long jobLogId, Integer handleCode, String handleMsg, Date finishHandleTime);

    long updateAlarmStatus(long failLogId, int oldStatus, int newStatus);

    void updateExecutorAddress(Long jobLogId, String address);

    void clearLog(Long jobId, Date clearBeforeTime, Integer clearBeforeNum);

    void clearLog(Long[] jobLogIds);

}
