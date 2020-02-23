package me.izhong.jobs.manage.impl.service.impl;

import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.common.service.CrudBaseServiceImpl;
import com.mongodb.client.result.UpdateResult;
import me.izhong.jobs.manage.impl.core.model.ZJobLog;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ZJobLogServiceImpl extends CrudBaseServiceImpl<Long,ZJobLog> implements ZJobLogService {
    @Override
    public long triggerCountByHandleCode(int successCode) {
        Query query = new Query();
        query.addCriteria(Criteria.where("triggerCode").is(successCode));
        return super.count(query,null,null);
    }

    @Override
    public List<Long> findFailJobLogIds() {
        Query query = new Query();
        Criteria c1 = Criteria.where("triggerCode").in(0,200).andOperator( Criteria.where("handleCode").is(0)  );
        Criteria c2 = Criteria.where("handleCode").is(200);
        Criteria dest = c1.orOperator(c2).not();
        query.addCriteria(dest);
        List<ZJobLog> ls = super.selectList(query,null,null);
        if(ls ==null || ls.size() == 0)
            return new ArrayList<>();
        return ls.stream().map(e->e.getJobId()).collect(Collectors.toList());

    }

    @Override
    public List<ZJobLog> findRunningJobs() {
        Query query = new Query();
        query.addCriteria(Criteria.where("handleCode").is(null));
        return super.selectList(query,null,null);
    }

    @Override
    public List<ZJobLog> findRunningJobs(Long jobId) {
        Assert.notNull(jobId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobId").is(jobId));
        query.addCriteria(Criteria.where("handleCode").is(null));
        return super.selectList(query,null,null);
    }

    @Override
    public List<ZJobLog> findJobLogByJobId(Long jobId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("jobId").is(jobId));
        return super.selectList(query,null,null);
    }

    @Transactional
    @Override
    public ZJobLog insertTriggerBeginMessage(Long jobId, Long jobGroupId, String jobDesc, Date triggerTime, String triggerType,
                                             Integer finalFailRetryCount,Long executorTimeout,String executorParam, String blockStrategy) {
        ZJobLog jobLog = new ZJobLog();
        jobLog.setJobId(jobId);
        jobLog.setJobGroupId(jobGroupId);
        jobLog.setJobDesc(jobDesc);
        jobLog.setTriggerTime(triggerTime);
        jobLog.setTriggerType(triggerType);
        jobLog.setExecutorFailRetryCount(finalFailRetryCount);
        jobLog.setExecutorTimeout(executorTimeout);
        jobLog.setBlockStrategy(blockStrategy);
        jobLog.setExecutorParam(executorParam);
        return super.insert(jobLog);
    }

    @Transactional
    @Override
    public void updateTriggerDoneMessage(Long jobLogId, String executorParam, Integer triggerCode, String triggerMsg) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        Update update = new Update();
        if(executorParam != null)
            update.set("executorParam",executorParam);
        update.set("triggerCode",triggerCode);
        update.set("triggerMsg",triggerMsg);
        update.set("updateTime",new Date());
        ZJobLog ur = mongoTemplate.findAndModify(query, update, options, ZJobLog.class);
        //log.info("返回新值:{},{}",ur.getTriggerCode(),ur.getTriggerMsg());
    }

    @Override
    public void updateProcessRemark(Long jobLogId, Long processResult, String processMessage) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        Update update = new Update();
        update.set("processResult",processResult);
        update.set("processMessage",processMessage);
        mongoTemplate.findAndModify(query, update, ZJobLog.class);
    }

    @Override
    public void updateHandleStartMessage(Long jobLogId, Date startTime) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        Update update = new Update();
        update.set("handleTime",startTime);
        update.set("updateTime",new Date());
        mongoTemplate.findAndModify(query, update, ZJobLog.class);
    }

    @Override
    public void updateHandleDoneMessage(Long jobLogId, Integer handleCode, String handleMsg) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        Update update = new Update();
        update.set("handleCode",handleCode);
        update.set("handleMsg",handleMsg);
        update.set("updateTime",new Date());
        mongoTemplate.findAndModify(query, update, ZJobLog.class);
    }

    @Override
    public long updateAlarmStatus(long failLogId, int oldStatus, int newStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(failLogId));
        query.addCriteria(Criteria.where("alarmStatus").is(oldStatus));

        Update update = new Update();
        update.set("alarmStatus",newStatus);
        update.set("updateTime",new Date());
        UpdateResult ur = mongoTemplate.updateMulti(query, update, ZJobLog.class);
        return ur.getModifiedCount();
    }

    @Transactional
    @Override
    public void updateExecutorAddress(Long jobLogId, String address) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);

        Update update = new Update();
        update.set("executorAddress",address);
        update.set("updateTime",new Date());
        mongoTemplate.findAndModify(query, update, options,ZJobLog.class);
    }


    public ZJobLog update(ZJobLog target) {
        //log.info("targetAddress:{}",target.getExecutorAddress());
        return super.update(target);
    }

    @Override
    public void clearLog(Long jobId, Date clearBeforeTime, Integer clearBeforeNum) {
        Query query = new Query();
        if(clearBeforeTime == null && clearBeforeNum == null)
            throw BusinessException.build("参数异常,清理日志的限制时间和限制数量不能同时为空");
        if(jobId != null)
            query.addCriteria(Criteria.where("jobId").is(jobId));
        if(clearBeforeTime !=null)
            query.addCriteria(Criteria.where("createTime").lte(clearBeforeTime));
        if(clearBeforeNum > 0) {
            query.skip(clearBeforeNum);
            query.with(new Sort(Sort.Direction.DESC, "createTime"));
        }
        query.addCriteria(Criteria.where("handleCode").ne(null));
        DeleteResult rt = mongoTemplate.remove(query, ZJobLog.class);
        log.info("已删除日志数量:{}",rt.getDeletedCount());
    }


    @Override
    public void clearLog(Long[] jobLogIds) {
        if(jobLogIds == null || jobLogIds.length == 0)
            return;
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").in(jobLogIds));
        DeleteResult rt = mongoTemplate.remove(query, ZJobLog.class);
        log.info("已删除日志数量:{}",rt.getDeletedCount());
    }
}
