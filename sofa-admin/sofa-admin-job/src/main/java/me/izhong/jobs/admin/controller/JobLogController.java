package me.izhong.jobs.admin.controller;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.Convert;
import me.izhong.common.util.DateUtil;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.util.StringUtil;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.admin.config.JobPermissions;
import me.izhong.jobs.admin.service.JobServiceReference;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.model.JobGroup;
import me.izhong.jobs.model.JobLog;
import me.izhong.jobs.model.LogResult;
import me.izhong.common.model.ReturnT;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/ext/djob/log")
@Slf4j
public class JobLogController {

	@Autowired(required = false)
	private JobServiceReference jobServiceReference;

	private static String prefix = "ext/djob";

	@RequestMapping("")
	public String index(HttpServletRequest request, Model model, Long jobId) {

		// 执行器列表
		List<JobGroup> jobGroupList_all =  jobServiceReference.jobService.selectAllJobGroup();
		model.addAttribute("groupList", jobGroupList_all);

		// 任务
		if (jobId!= null) {
			Job jobInfo = jobServiceReference.jobService.findByJobId(jobId);
			if (jobInfo == null) {
				throw new RuntimeException("未找到");
			}
			JobGroup JobGroup = jobServiceReference.jobService.findJobGroup(jobInfo.getJobGroupId());

			model.addAttribute("jobGroupId", JobGroup.getGroupId());
			model.addAttribute("jobDesc", jobInfo.getJobDesc());
		}

		return prefix + "/jobLog";
	}

	
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<JobLog> pageList(HttpServletRequest request, String jobDesc, Long jobGroupId,
									  String status,Integer handleCode, String processResult, String filterTime) {
		PageRequest pageRequest = PageRequestUtil.fromRequest(request);
		// parse param
		Date triggerTimeStart = null;
		Date triggerTimeEnd = null;
		if (filterTime!=null && filterTime.trim().length()>0) {
			String[] temp = filterTime.split(" - ");
			if (temp!=null && temp.length == 2) {
//				triggerTimeStart = DateUtil.parseDateTime(temp[0]);
//				triggerTimeEnd = DateUtil.parseDateTime(temp[1]);
			}
		}

		JobLog jLog = new JobLog();
		if(StringUtil.isNotBlank(jobDesc))
			jLog.setJobDesc(jobDesc);
		if(jobGroupId != null)
			jLog.setJobGroupId(jobGroupId);
		if(handleCode != null)
			jLog.setHandleCode(handleCode);
		if(StringUtils.equals(processResult,"HAVE_JOB")){
			jLog.setProcessResult(1L);
		}
		return jobServiceReference.jobService.logPageList(pageRequest,jLog);
	}

	@RequiresPermissions(JobPermissions.JobInfo.LOG_VIEW)
	@RequestMapping("/detail/{jobLogId}")
	public String logDetailPage(@PathVariable Long jobLogId, Model model){
		JobLog jobLog = jobServiceReference.jobService.findJobLogByJobLogId(jobLogId);
		if (jobLog == null) {
			throw new RuntimeException("日志异常");
		}
		model.addAttribute("jobLog", jobLog);
		return prefix +  "/jobLogResult";
	}

	@RequiresPermissions(JobPermissions.JobInfo.LOG_VIEW)
	@RequestMapping("/logDetailPage")
	public String logDetailPage(long logId, Model model){

		// base check
		JobLog jobLog = jobServiceReference.jobService.findJobLogByJobLogId(logId);
		if (jobLog == null) {
            throw new RuntimeException("日志异常,日志未找到");
		}
		String content = "";
		int lineNumber = 1;
		if(jobLog.getTriggerTime() != null ) {
			LogResult logResult = jobServiceReference.jobService.catLog(jobLog.getJobId(), logId, jobLog.getTriggerTime().getTime(), 1);
			content = logResult.getLogContent();
			lineNumber = logResult.getToLineNum() + 1;
		}
		model.addAttribute("jobLogContent", content);
		model.addAttribute("jobLogFromLineNumber", lineNumber);
		model.addAttribute("jobLog", jobLog);
		return prefix +  "/jobLogDetail";
	}

	@RequiresPermissions(JobPermissions.JobInfo.LOG_VIEW)
	@RequestMapping("/logDetailCat")
	@AjaxWrapper
	public LogResult logDetailCat(String executorAddress, long triggerTime, long jobId, long jobLogId, int fromLineNum){
		try {
			LogResult logResult = jobServiceReference.jobService.catLog(jobId, jobLogId, triggerTime, fromLineNum);
			// is end
//            if (logResult.getContent()!=null && logResult.getContent().getFromLineNum() > logResult.getContent().getToLineNum()) {
//                JobLog jobLog = jobServiceReference.jobService.findJobLogByJobLogId(jobLogId);
//                if (jobLog.getHandleCode() > 0) {
//                    logResult.getContent().setEnd(true);
//                }
//            }

			return logResult;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.build(e.getMessage());
		}
	}

	@Log(title = "定时任务", businessType = BusinessType.OPERATE)
	@RequiresPermissions(JobPermissions.JobInfo.OPERATE)
	@RequestMapping("/kill")
	@AjaxWrapper
	public ReturnT<String> kill(long jobLogId){
		// base check
		JobLog jobLog = jobServiceReference.jobService.findJobLogByJobLogId(jobLogId);
		Job jobInfo = jobServiceReference.jobService.findByJobId(jobLog.getJobId());
		if (jobInfo==null) {
			throw BusinessException.build("任务不存在");
		}
		if (0 != jobLog.getTriggerCode()) {
			throw BusinessException.build("日志执行异常，不kill");
		}

		// request of kill
		ReturnT<String> runResult = null;
		try {
			runResult = jobServiceReference.jobService.kill(jobLog.getJobLogId());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			runResult = new ReturnT<String>(500, e.getMessage());
		}
		return runResult;
	}

	@Log(title = "定时任务日志", businessType = BusinessType.DELETE)
	@RequiresPermissions(JobPermissions.JobInfo.LOG_CLEAN)
	@RequestMapping("/clearLog")
	@AjaxWrapper
	public ReturnT<String> clearLog(Long jobId, Integer type){
		if(type == null)
			throw BusinessException.build("type不能为空");
		Date clearBeforeTime = null;
		int clearBeforeNum = 0;
		if (type == 1) {
			clearBeforeTime = DateUtil.addMonths(new Date(), -1);	// 清理一个月之前日志数据
		} else if (type == 2) {
			clearBeforeTime = DateUtil.addMonths(new Date(), -3);	// 清理三个月之前日志数据
		} else if (type == 3) {
			clearBeforeTime = DateUtil.addMonths(new Date(), -6);	// 清理六个月之前日志数据
		} else if (type == 4) {
			clearBeforeTime = DateUtil.addYears(new Date(), -1);	// 清理一年之前日志数据
		} else if (type == 5) {
			clearBeforeNum = 100;		// 清理100条以前日志数据
		} else if (type == 6) {
			clearBeforeNum = 10000;		// 清理一万条以前日志数据
		} else if (type == 7) {
			clearBeforeNum = 30000;		// 清理三万条以前日志数据
		} else if (type == 8) {
			clearBeforeNum = 100000;	// 清理十万条以前日志数据
		} else if (type == 9) {
			clearBeforeNum = 0;			// 清理所有日志数据
		} else {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "类型异常");
		}

		jobServiceReference.jobService.clearLog(jobId, clearBeforeTime, clearBeforeNum);
		return ReturnT.SUCCESS;
	}

	@Log(title = "定时任务日志", businessType = BusinessType.DELETE)
	@RequiresPermissions(JobPermissions.JobInfo.LOG_CLEAN)
	@RequestMapping("/clearLogByIds")
    @AjaxWrapper
    public ReturnT<String> clearLog(String jobLogIds){
        jobServiceReference.jobService.clearLog(Convert.toLongArray(jobLogIds));
        return ReturnT.SUCCESS;
    }

}
