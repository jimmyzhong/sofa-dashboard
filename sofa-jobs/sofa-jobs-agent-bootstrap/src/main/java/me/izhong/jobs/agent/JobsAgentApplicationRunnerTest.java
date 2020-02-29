package me.izhong.jobs.agent;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.agent.job.ExecGrooyScript;
import me.izhong.jobs.agent.job.context.ScriptRunContext;
import me.izhong.jobs.agent.log.ConsoleLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        MongoAutoConfiguration.class})
@Slf4j
public class JobsAgentApplicationRunnerTest {
    public static void main(String[] args) {
        System.setProperty("spring.main.web-application-type","NONE");
        SpringApplication.run(JobsAgentApplicationRunnerTest.class, args);

        String file = "/Users/jimmy/space/sofa-dashboard/sofa-jobs/sofa-jobs-agent-bootstrap/src/main/groovy/BILL统计时间.groovy";

        //String file = "H:\\space\\xxx\\sofa-dashboard\\sofa-jobs\\sofa-jobs-agent-bootstrap\\src\\main\\groovy\\读取清分文件.groovy";
        //初始化运行环境
        ScriptRunContext context = new ScriptRunContext();
        context.setJobId(0L);
        context.setTriggerId(0L);
        //传文件路径主要是为了断点
        context.setScriptFile(new File(file));

        context.setTimeout(-1);
        context.setLog(new ConsoleLog());

        Map<String, String> envs = new HashMap<>();
        context.setEnvs(envs);
        Map<String, Object> params = new HashMap<>();
        params.put("log","logv");
        context.setParams(params);


        ExecGrooyScript execGrooyScript = new ExecGrooyScript();
        try {
            int r = execGrooyScript.execute(context);
            log.info("执行返回{}", r);
        } catch (Exception e) {
            log.error("",e);
        }
        System.exit(0);
    }

}
