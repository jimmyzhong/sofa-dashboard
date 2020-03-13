package me.izhong.jobs.agent.util;

import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.model.JobStats;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.Assert;

public class JobStatsUtil {

    public static String insertOrUpdate(String key, String type, String value1){
        JobStats js = insertOrUpdate(key,type,value1,null);
        if(js!=null)
            return js.getValue1();
        return null;
    }

    public static JobStats insertOrUpdate(String key, String type, String value1,String value2){
        return insertOrUpdate(key,type,value1,value2,null);
    }

    public static JobStats insertOrUpdate(String key, String type, String value1, String value2,String value3){
        Assert.notNull(key,"key不能为空");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        JobStats jobStats = new JobStats();
        jobStats.setKey(key);
        jobStats.setType(type);
        jobStats.setValue1(value1);
        jobStats.setValue2(value2);
        jobStats.setValue3(value3);
        return facade.insertOrUpdateJobStats(jobStats);
    }

    public static String getValue1(String key){
        Assert.notNull(key,"key不能为空");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        JobStats st = facade.findJobStatsByKey(key);
        if(st != null)
            return st.getValue1();
        return null;
    }

    public static JobStats get(String key){
        Assert.notNull(key,"key不能为空");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        JobStats st = facade.findJobStatsByKey(key);
        return st;
    }

    public static boolean checkExist(String key){
        Assert.notNull(key,"key不能为空");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        return facade.findJobStatsByKey(key) != null;
    }

    public static boolean remove(String key){
        Assert.notNull(key,"key不能为空");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        return facade.deleteJobStats(key);
    }

}
