package me.izhong.jobs.manage;

import me.izhong.jobs.model.LogResult;
import me.izhong.common.model.ReturnT;

import java.util.Map;

public interface IJobAgentMngFacade {

    ReturnT<String> kill(Long jobId, Long triggerId);

    ReturnT<String> status(Long jobId, Long triggerId);

    ReturnT<String> trigger(Long jobId, Long triggerId, Long timeout,Map<String, String> envs, Map<String,String> params);

    LogResult catLog( Long jobId,  Long triggerId, long triggerTime, int fromLineNum);

}
