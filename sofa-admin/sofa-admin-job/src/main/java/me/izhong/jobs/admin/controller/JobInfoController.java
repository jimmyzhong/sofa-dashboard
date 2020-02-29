package me.izhong.jobs.admin.controller;

import me.izhong.common.util.Convert;
import me.izhong.common.util.CronUtil;
import me.izhong.common.util.DateUtil;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.jobs.admin.config.JobPermissions;
import me.izhong.jobs.admin.service.JobServiceReference;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.model.JobGroup;
import me.izhong.jobs.type.TriggerTypeEnum;
import me.izhong.common.model.ReturnT;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/ext/djob")
public class JobInfoController {

	private String prefix = "ext/djob";

	@Autowired(required = false)
	private JobServiceReference jobServiceReference;

	@GetMapping()
	public String job(Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {
		List<JobGroup> jobGroupList = jobServiceReference.jobService.selectAllJobGroup();
		if (jobGroupList==null || jobGroupList.size()==0) {
		//	throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}
		model.addAttribute("groupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);
		return prefix + "/job";
	}

	@RequiresPermissions(JobPermissions.JobInfo.VIEW)
	@RequestMapping("/view")
	@AjaxWrapper
	public Job view(Long jobId) {
		return jobServiceReference.jobService.findByJobId(jobId);
	}

	@RequiresPermissions(JobPermissions.JobInfo.VIEW)
	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<Job> pageList(HttpServletRequest request, Job ino) {
		ino.setIsDelete(false);
		PageModel<Job>  pm = jobServiceReference.jobService.pageList(PageRequestUtil.fromRequest(request),ino);
		if(pm != null){
			List<Job> jobs = pm.getRows();
			if(jobs != null) {
				jobs.forEach(e -> {
					e.setTriggerLastTimeString(DateUtil.parseLongToFullSting(e.getTriggerLastTime()));
					e.setTriggerNextTimeString(DateUtil.parseLongToFullSting(e.getTriggerNextTime()));
				});
			}
		}
		return pm;
	}

	@GetMapping("/add")
	public String add(Model model) {
		model.addAttribute("groupList",jobServiceReference.jobService.selectAllJobGroup());
		return prefix + "/add";
	}

    @Log(title = "定时任务", businessType = BusinessType.ADD)
    @RequiresPermissions(JobPermissions.JobInfo.ADD)
	@PostMapping("/add")
	@AjaxWrapper
	public void add(Job jobInfo) {
		if(jobInfo.getExecutorFailRetryCount() == null) {
			jobInfo.setExecutorFailRetryCount(0);
		}
		if(jobInfo.getExecutorTimeout() == null) {
			jobInfo.setExecutorTimeout(30000L);
		}
		if(jobInfo.getTriggerStatus() == null) {
			jobInfo.setTriggerStatus(1L);
		}
        jobInfo.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        jobInfo.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
		ReturnT<String> rObj = jobServiceReference.jobService.add(jobInfo);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}

	@GetMapping("/edit/{jobId}")
	public String edit(@PathVariable("jobId") Long jobId,Model model) {
		if(jobId == null){
			throw BusinessException.build("jobId 不能为空");
		}
		model.addAttribute("groupList",jobServiceReference.jobService.selectAllJobGroup());
		Job job = jobServiceReference.jobService.findByJobId(jobId);
		if(job == null) {
			throw BusinessException.build(String.format("任务不存在%s",jobId));
		}
		job.setTriggerNextTimeString(DateUtil.parseLongToFullSting(job.getTriggerNextTime()));
		model.addAttribute("job",job);
		return prefix + "/edit";
	}

    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @RequiresPermissions(JobPermissions.JobInfo.EDIT)
	@PostMapping("/edit")
	@AjaxWrapper
	public void update(Job jobInfo) {
		Job exists_jobInfo = jobServiceReference.jobService.findByJobId(jobInfo.getJobId());
		if (exists_jobInfo == null) {
			throw BusinessException.build("任务不存在");
		}

		//只修改允许修改的内容
		exists_jobInfo.setJobGroupId(jobInfo.getJobGroupId());
		exists_jobInfo.setJobCron(jobInfo.getJobCron());
		exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
		exists_jobInfo.setAuthor(jobInfo.getAuthor());
		exists_jobInfo.setAlarmEmail(jobInfo.getAlarmEmail());
		exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
		exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
		exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
		exists_jobInfo.setConcurrentSize(jobInfo.getConcurrentSize());
		exists_jobInfo.setExecutorTimeout(jobInfo.getExecutorTimeout());
		exists_jobInfo.setExecutorFailRetryCount(jobInfo.getExecutorFailRetryCount());
		exists_jobInfo.setChildJobId(jobInfo.getChildJobId());
		exists_jobInfo.setRemark(jobInfo.getRemark());

		exists_jobInfo.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
		ReturnT<String> rObj = jobServiceReference.jobService.update(exists_jobInfo);
		if(ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}

	@Log(title = "定时任务", businessType = BusinessType.DELETE)
	@RequiresPermissions(JobPermissions.JobInfo.REMOVE)
	@RequestMapping("/remove")
	@AjaxWrapper
	public void remove(String ids) {
		Long[] jobIds = Convert.toLongArray(ids);
		for(Long jobId : jobIds) {
			ReturnT<String> rObj = jobServiceReference.jobService.remove(jobId);
			if (ReturnT.SUCCESS_CODE != rObj.getCode()) {
				throw BusinessException.build(rObj.getMsg());
			}
		}
	}

	@Log(title = "定时任务", businessType = BusinessType.OPERATE)
	@RequiresPermissions(JobPermissions.JobInfo.OPERATE)
	@RequestMapping("/stop")
	@AjaxWrapper
	public void stop(Long jobId) {
		ReturnT<String> rObj = jobServiceReference.jobService.disable(jobId);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}

	@Log(title = "定时任务", businessType = BusinessType.OPERATE)
	@RequiresPermissions(JobPermissions.JobInfo.OPERATE)
	@RequestMapping("/start")
	@AjaxWrapper
	public void start(Long jobId) {
		ReturnT<String> rObj = jobServiceReference.jobService.enable(jobId);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}

	@Log(title = "定时任务", businessType = BusinessType.OPERATE)
	@RequiresPermissions(JobPermissions.JobInfo.OPERATE)
	@RequestMapping("/trigger")
	@AjaxWrapper
	public void triggerJob(Long jobId, String executorParam) {
		if(StringUtils.isBlank(executorParam)) {
			executorParam = "{\"\":\"\"}";
		}
		ReturnT<String> rObj = jobServiceReference.jobService.trigger(jobId,TriggerTypeEnum.MANUAL,-1,executorParam);
		if( ReturnT.SUCCESS_CODE != rObj.getCode()){
			throw BusinessException.build(rObj.getMsg());
		}
	}

	@PostMapping("/checkCronExpressionIsValid")
	@ResponseBody
	public boolean checkCronExpressionIsValid(String jobCron)
	{
		return CronUtil.isValid(jobCron);
	}
	
}
