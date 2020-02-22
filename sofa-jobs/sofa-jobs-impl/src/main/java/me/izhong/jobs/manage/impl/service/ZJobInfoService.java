package me.izhong.jobs.manage.impl.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.common.model.ReturnT;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ZJobInfoService extends CrudBaseService<Long,ZJobInfo> {
    List<ZJobInfo> scheduleJobQuery(long maxNextTime);
    List<ZJobInfo> selectByJobGroupId(Long jobGroupId);

    void scheduleUpdate(ZJobInfo jobInfo);

    void updateWaitAgain(Long jobId, Boolean waitAgain);

    void updateRunningTriggers(Long jobId, List<Long> runningTriggerIds);

    void updateJobScriptId(Long jobId, Long scriptId);

    void updateJobNextTriggerTime(Long jobId, Date triggerNextTime);

    PageModel<ZJobInfo> pageList(PageRequest request, ZJobInfo jobInfo);

    List<ZJobInfo> findRunningJobs();

    ZJobInfo addJob(ZJobInfo jobInfo);

    ZJobInfo updateJob(ZJobInfo jobInfo);

    long removeJob(long id);

    /**
     * start job
     *
     * @param id
     * @return
     */
    long enableJob(long id);

    /**
     * stop job
     *
     * @param id
     * @return
     */
    long disableJob(long id);

    /**
     * dashboard info
     *
     * @return
     */
    public Map<String,Object> dashboardInfo();

    /**
     * chart info
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public ReturnT<Map<String,Object>> chartInfo(Date startDate, Date endDate);

}
