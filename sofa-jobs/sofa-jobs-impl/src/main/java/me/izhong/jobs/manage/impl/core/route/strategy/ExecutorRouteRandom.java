package me.izhong.jobs.manage.impl.core.route.strategy;

import me.izhong.jobs.manage.impl.core.route.ExecutorRouter;
import me.izhong.jobs.model.TriggerParam;
import me.izhong.common.model.ReturnT;

import java.util.List;
import java.util.Random;

public class ExecutorRouteRandom extends ExecutorRouter {

    private static Random localRandom = new Random();

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return new ReturnT<String>(address);
    }

}
