package me.izhong.jobs.agent.util;

import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.manage.IJobMngFacade;
import org.springframework.util.Assert;

public class JobConfigUtil {

    public static String getConfigValue(String configKey){
        Assert.notNull(configKey,"configKey不能为空");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        String st = facade.findConfigByKey(configKey);
        return st;
    }

}
