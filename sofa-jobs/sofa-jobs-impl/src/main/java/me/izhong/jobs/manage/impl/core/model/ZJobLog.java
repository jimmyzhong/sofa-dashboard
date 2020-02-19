package me.izhong.jobs.manage.impl.core.model;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.Search;
import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.PrimaryId;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_djob_log")
public class ZJobLog extends TimedBasedEntity implements Serializable {

	@AutoId
	@PrimaryId
	@Indexed(unique = true)
	private Long jobLogId;

	@Search(op = Search.Op.REGEX)
	private String jobDesc;
	@Search
	private Long jobGroupId;

	@Indexed
	private Long jobId;

	// execute info
	private String executorAddress;
	private String executorParam;
	private Long executorTimeout;
	private Integer executorFailRetryCount;
	private String blockStrategy;
	
	// trigger info
	private Date triggerTime;
	private String triggerType;
	private Integer triggerCode;
	private String triggerMsg;
	
	// handle info
	private Date handleTime;
	private Date finishHandleTime;
	private String costHandleTime;

	@Search
	private Integer handleCode;
	private String handleMsg;

	@Search(op = Search.Op.GTE)
	private Long processResult;
	private String processMessage;

	// alarm info
	private int alarmStatus;

    public String getHandleMsg() {
        return handleMsg;
    }

    public void setHandleMsg(String handleMsg) {
        if(StringUtils.isBlank(handleMsg)){
            //log.error("设置handleMsg为空");
        }
        this.handleMsg = handleMsg;
    }
}
