package me.izhong.jobs.manage.impl.core.model;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.Search;
import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.PrimaryId;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_djob_info")
public class ZJobInfo extends TimedBasedEntity implements Serializable {

	@AutoId
	@PrimaryId
	@Indexed(unique = true)
	private Long jobId;				// 主键ID

	@Search
	private Long jobGroupId;		// 执行器主键ID
	private String jobCron;		// 任务执行CRON表达式
	private Long jobScriptId;

	@Search(op = Search.Op.REGEX)
	private String jobDesc;

	private String author;		// 负责人
	private String alarmEmail;	// 报警邮件

	private String executorRouteStrategy;	// 执行器路由策略
	private String executorParam;		    // 执行器，任务参数
	private String executorBlockStrategy;	// 阻塞处理策略
	private Long executorTimeout;     		// 任务执行超时时间，单位秒
	private Integer executorFailRetryCount;		// 失败重试次数

	private String childJobId;		// 子任务ID，多个逗号分隔

	@Search
	private Long triggerStatus;		// 调度状态：1-停止，0-运行
	private Long triggerLastTime;	// 上次调度时间
	private Long triggerNextTime;	// 下次调度时间

	//正在执行的triggerId
	private List<Long> runningTriggerIds = new ArrayList<>();

	private Boolean wakeAgain;  //如果过true，说明后续有这个任务的执行因为前一个任务在执行中没有调度起来

	private Integer concurrentSize; //并发执行的数量
}
