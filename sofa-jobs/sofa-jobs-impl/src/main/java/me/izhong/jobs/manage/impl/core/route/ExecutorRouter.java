package me.izhong.jobs.manage.impl.core.route;


import me.izhong.jobs.model.TriggerParam;
import me.izhong.common.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public abstract class ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    /**
     * route address
     *
     * @param addressList
     * @return  ReturnT.content=address
     */
    public abstract ReturnT<String> route(TriggerParam triggerParam, List<String> addressList);

}
