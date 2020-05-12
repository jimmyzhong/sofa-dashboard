package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Job implements Serializable {

    private Long jobId;
    private Long jobGroupId;
    private String jobGroup;

    private Long jobScriptId;

    private String jobCron;
    private String jobDesc;

    private String author;
    private String alarmEmail;
    private String remark;

    private String executorRouteStrategy;	// 执行器路由策略
    private String executorParam;		    // 执行器，任务参数
    private String executorBlockStrategy;	// 阻塞处理策略
    private Long executorTimeout;     		// 任务执行超时时间，单位秒
    private Integer executorFailRetryCount;		// 失败重试次数

    private String childJobId;		// 子任务ID，多个逗号分隔

    private Long triggerStatus;		// 调度状态：0-停止，1-运行
    private Long triggerLastTime;	// 上次调度时间
    private Long triggerNextTime;	// 下次调度时间

    private String triggerLastTimeString;	// 上次调度时间
    private String triggerNextTimeString;	// 下次调度时间

    private String tIds;

    private Integer concurrentSize; //并发执行的数量

    private Boolean isDelete;
    private Date createTime;
    private String createBy;
    private Date updateTime;
    private String updateBy;

}
