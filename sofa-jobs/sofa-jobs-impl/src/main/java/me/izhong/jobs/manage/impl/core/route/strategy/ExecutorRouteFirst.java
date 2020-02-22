package me.izhong.jobs.manage.impl.core.route.strategy;

import me.izhong.jobs.manage.impl.core.route.ExecutorRouter;
import me.izhong.jobs.model.TriggerParam;
import me.izhong.common.model.ReturnT;

import java.util.List;


public class ExecutorRouteFirst extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList){
        return new ReturnT<String>(addressList.get(0));
    }

}
