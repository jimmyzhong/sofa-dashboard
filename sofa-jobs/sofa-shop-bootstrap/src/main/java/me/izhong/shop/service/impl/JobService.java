package me.izhong.shop.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.JobDao;
import me.izhong.shop.entity.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class JobService {
    @Autowired
    JobDao jobDao;

    @Transactional
    public boolean acquireJob(String jobName, LocalDateTime lastRunTime, LocalDateTime updateLastRunTime) {
        Job job = jobDao.findFirstByNameAndLastRunTimeBefore(jobName, lastRunTime);
        if (job == null) return false;

        log.info("trying to get job " + jobName);
        job = jobDao.selectJobForUpdate(job.getId());
        if (job.getLastRunTime().compareTo(lastRunTime) > 0) {
            return false;
        }
        job.setLastRunTime(updateLastRunTime);
        jobDao.save(job);
        return true;
    }
}
