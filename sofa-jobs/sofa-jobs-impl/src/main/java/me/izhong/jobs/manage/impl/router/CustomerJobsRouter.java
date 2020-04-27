package me.izhong.jobs.manage.impl.router;

import com.alipay.sofa.rpc.bootstrap.ConsumerBootstrap;
import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.client.Router;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.manage.IJobAgentMngFacade;
import me.izhong.jobs.manage.impl.core.model.ZJobLog;
import me.izhong.jobs.manage.impl.core.util.SpringUtil;
import me.izhong.jobs.manage.impl.service.ZJobInfoService;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;

@Extension(value = "customerRouter")
@AutoActive(consumerSide = true)
@Slf4j
public class CustomerJobsRouter extends Router {

    @Override
    public void init(ConsumerBootstrap consumerBootstrap) {

    }

    @Override
    public boolean needToLoad(ConsumerBootstrap consumerBootstrap) {
        String inerfaceId = consumerBootstrap.getConsumerConfig().getInterfaceId();
        if(StringUtils.equals(inerfaceId,IJobAgentMngFacade.class.getName())) {
            log.info("begin effect");
            return true;
        }
        return false;
    }

    @Override
    public List<ProviderInfo> route(SofaRequest request, List<ProviderInfo> providerInfos) {

        try {
            String interfaceName = request.getInterfaceName();
            String method = request.getMethod().getName();
            ZJobInfoService jobInfoService = SpringUtil.getBean(ZJobInfoService.class);
            ZJobLogService jobLogService = SpringUtil.getBean(ZJobLogService.class);
//            路由地址: interfaceName:me.izhong.jobs.manage.IJobAgentMngFacade method:status  [bolt://172.30.251.92:13303?version=1.0&accepts=100000&appName=job-agent&weight=100&language=java
//&pid=26913&interface=me.izhong.jobs.manage.IJobAgentMngFacade&timeout=0&serialization=hessian2&protocol=bolt&delay=-1&dynamic=true&startTime=1574140442562&id=jobAgentMngImpl&uniqueId=1&rpcVer=50504]

            log.info("路由地址: interfaceName:{} method:{}", interfaceName, method);
            if ("trigger".equals(method)) {

                Object[] args = request.getMethodArgs();
                Long jobId = (Long) args[0];
                Long triggerId = (Long) args[1];

                List<String> allIPs = new ArrayList<>();
                Map<String,Integer> map = new HashMap<>();
                providerInfos.forEach(e->{
                    String avaIp = e.getHost();
                    map.put(avaIp,0);
                    allIPs.add(avaIp);
                });

                List<ZJobLog> runningJobs = jobLogService.findRunningJobs(jobId);
                if(runningJobs != null) {
                    runningJobs.forEach(e->{
                        String ip = e.getExecutorAddress();
                        if(StringUtils.isNotBlank(ip)) {
                            Integer v = map.get(ip);
                            if(v != null) {
                                map.put(ip,v.intValue() + 1);
                            }
                        }
                    });
                }
                //选最小
                final int[] min = {Integer.MAX_VALUE};
                map.forEach( (k,v) -> {
                    if(v != null) {
                        if(v.intValue() < min[0]) {
                            min[0] = v.intValue();
                        }
                    }
                });

                List<String> selectedIps = new ArrayList<>();
                map.forEach( (k,v) -> {
                    if(v != null) {
                        if(v.intValue() == min[0] && !selectedIps.contains(k)) {
                            selectedIps.add(k);
                        }
                    }
                });
                int randInt = RandomUtils.nextInt() % selectedIps.size();
                String selectedIp = selectedIps.get(randInt);
                AtomicReference<ProviderInfo> pi = new AtomicReference<>();
                providerInfos.forEach(e->{
                    String avaIp = e.getHost();
                    if(StringUtils.equals(selectedIp,avaIp)) {
                        pi.set(e);
                    }
                });
                log.info("本次候选ip地址 {} 最闲IP {}, 选择ip地址 {} , 选择的pi {}", allIPs, selectedIps, selectedIp, pi.get());

                //
//                int providerSize = providerInfos.size();
//                int randInt = RandomUtils.nextInt() % providerSize;
//                ProviderInfo pi = providerInfos.get(randInt);
//                log.info("按照路由策略执行trigger,总共{}选择{}地址是{}",providerSize,randInt,pi.getHost());
//

//                jobInfoService.selectByPId(jobId);

                jobLogService.updateExecutorAddress(triggerId, pi.get().getHost());
               // log.info("按照路由策略执行trigger,总共{}选择{}地址是{}",providerInfos,randInt, pi.get().getHost());

                return new ArrayList<ProviderInfo>() {{
                    add(pi.get());
                }};

            } else if (StringUtils.equalsAny(method, "catLog", "kill","status")) {
                Object[] args = request.getMethodArgs();
                Long jobId = (Long) args[0];
                Long triggerId = (Long) args[1];
                jobInfoService.selectByPId(jobId);

                ZJobLog jobLog = jobLogService.selectByPId(triggerId);
                if (jobLog == null) {
                    log.info("任务{} 日志没有找到 {}", jobId, triggerId);
                    throw new RuntimeException("任务" + jobId + "日志没有找到 " + triggerId);
                }
                String jobAddress = jobLog.getExecutorAddress();
                if (StringUtils.isBlank(jobAddress)) {
                    log.info("任务{} 日志{} 的执行地址为空", jobId, triggerId);
                }
                log.info("任务{}发送到{}执行{}",jobId,jobAddress,method);
                return providerInfos.stream().filter(e -> e.getHost().equals(jobAddress)).collect(toList());
            }
        }catch (Exception e) {
            log.error("route err:", e);
        }
        return providerInfos;
    }
}