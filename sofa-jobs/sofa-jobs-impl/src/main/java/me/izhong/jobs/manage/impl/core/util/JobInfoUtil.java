package me.izhong.jobs.manage.impl.core.util;

import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.jobs.model.Job;
import org.springframework.beans.BeanUtils;

public class JobInfoUtil {

    public static Job toRpcBean(ZJobInfo db){
        if(db == null)
            return null;
        Job job = new Job();
        BeanUtils.copyProperties(db,job);
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
