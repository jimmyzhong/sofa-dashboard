package me.izhong.jobs.manage.impl.core.util;

import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.jobs.model.Job;
import org.springframework.beans.BeanUtils;

import java.util.stream.Collectors;

public class JobInfoUtil {

    public static Job toRpcBean(ZJobInfo db){
        if(db == null)
            return null;
        Job job = new Job();
        BeanUtils.copyProperties(db,job);
        if(db.getRunningTriggerIds() !=null) {
            job.setTIds(String.join("-",db.getRunningTriggerIds().stream().map(e->e.toString()).collect(Collectors.toList())));
        }
        return job;
    }

    public static ZJobInfo toDbBean(Job job){
        if(job ==null)
            return null;
        ZJobInfo xInfo = new ZJobInfo();
        BeanUtils.copyProperties(job,xInfo);
        return xInfo;
    }


}
