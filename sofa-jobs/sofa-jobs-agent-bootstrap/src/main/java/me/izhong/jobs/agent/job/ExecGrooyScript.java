package me.izhong.jobs.agent.job;


import me.izhong.jobs.agent.bean.JobsConfigBean;
import me.izhong.jobs.agent.job.context.ScriptRunContext;
import me.izhong.jobs.agent.log.AgentLog;
import me.izhong.jobs.agent.util.ContextUtil;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@DependsOn("contextUtil")
@Component
@Slf4j
public class ExecGrooyScript implements IBatch {

	@Autowired
	private JobsConfigBean configBean;

	@Override
	public String scriptType() {
		return "groovy";
	}

	@Override
	public int execute(ScriptRunContext context) throws Exception {

		String script = context.getScript();
		File scriptPath = context.getScriptFile();

		if(StringUtils.isBlank(script) && scriptPath == null){
			log.info("script:",context.getScript());
			throw new Exception("ExecGrooyScript参数错误:脚本名称不能为空");
		}

		AgentLog logger = context.getLog();

		Logger slfLog = LoggerFactory.getLogger("脚本日志");
		log.info("Groovy脚本收到执行参数:{}",context.getParams());
		Binding binding = new Binding();
		binding.setProperty("ac", ContextUtil.getApplicationContext());
		binding.setProperty("remoteLog", logger);
		binding.setProperty("log", slfLog);
		//binding.setProperty("jobId", context.getJobId());
		//binding.setProperty("triggerId", context.getTriggerId());
		//binding.setProperty("envs", context.getEnvs());
		Map<String,Object> p = context.getParams();
		if(p == null )
			p = new HashMap<>();
		p.put("jobId",context.getJobId());
		p.put("jobTriggerId",context.getTriggerId());
		p.put("jobTimeout",context.getTimeout());
		binding.setProperty("params", p);

		GroovyShell shell = new GroovyShell(binding);

		log.info("执行Groovy脚本,注入参数:{}",context.getParams());
		Object result;
		if(scriptPath != null) {
			result = shell.evaluate(scriptPath);
		} else {
			result = shell.evaluate(script);
		}
		log.info("Groovy脚本返回：{}", result);
		if (result instanceof Integer)
			return (Integer) result;
		else
			return -1;
	}

}
