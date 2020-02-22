package me.izhong.jobs.manage.impl.core.route.strategy;

import me.izhong.jobs.manage.impl.core.route.ExecutorRouter;
import me.izhong.jobs.model.TriggerParam;
import me.izhong.common.model.ReturnT;


import java.util.List;

/**
 */
public class ExecutorRouteBusyover extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        StringBuffer idleBeatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ReturnT<String> idleBeatResult = null;
            try {
                //ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(address);
                //idleBeatResult = executorBiz.idleBeat(triggerParam.getJobId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                idleBeatResult = new ReturnT<String>(ReturnT.FAIL_CODE, ""+e );
            }
            idleBeatResultSB.append( (idleBeatResultSB.length()>0)?"<br><br>":"")
                    .append("心跳" + "：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(idleBeatResult.getCode())
                    .append("<br>msg：").append(idleBeatResult.getMsg());

            // beat success
            if (idleBeatResult.getCode() == ReturnT.SUCCESS_CODE) {
                idleBeatResult.setMsg(idleBeatResultSB.toString());
                idleBeatResult.setContent(address);
                return idleBeatResult;
            }
        }

        return new ReturnT<String>(ReturnT.FAIL_CODE, idleBeatResultSB.toString());
    }

}
