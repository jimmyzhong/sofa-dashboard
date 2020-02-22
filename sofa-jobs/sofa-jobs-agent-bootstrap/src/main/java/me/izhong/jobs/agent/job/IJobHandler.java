package me.izhong.jobs.agent.job;

import me.izhong.jobs.agent.bean.JobContext;
import me.izhong.common.model.ReturnT;

public abstract class IJobHandler {


    /**
     * success
     */
    public static final ReturnT<String> SUCCESS = new ReturnT<String>(0, null);
    /**
     * fail
     */
    public static final ReturnT<String> FAIL = new ReturnT<String>(500, null);
    /**
     * fail timeout
     */
    public static final ReturnT<String> FAIL_TIMEOUT = new ReturnT<String>(502, null);


    /**
     * execute handler, invoked when executor receives a scheduling request
     *
     * @return
     * @throws Exception
     */
    public abstract ReturnT<String> execute(JobContext jobContext) throws Exception;


    /**
     * init handler, invoked when JobThread init
     */
    public void init() {
        // do something
    }


    /**
     * destroy handler, invoked when JobThread destroy
     */
    public void destroy() {
        // do something
    }
}