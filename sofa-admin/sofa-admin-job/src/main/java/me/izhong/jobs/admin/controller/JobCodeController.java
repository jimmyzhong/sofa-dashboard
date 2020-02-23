package me.izhong.jobs.admin.controller;

import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.admin.config.JobPermissions;
import me.izhong.jobs.admin.service.JobServiceReference;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.model.JobScript;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/ext/djob")
public class JobCodeController {

	private String prefix = "ext/djob";

	@Autowired(required = false)
	private JobServiceReference jobServiceReference;

	@RequiresPermissions(JobPermissions.JobInfo.CODE_VIEW)
	@RequestMapping("/code")
	public String index(HttpServletRequest request, Model model, long jobId) {
		Job jobInfo = jobServiceReference.jobService.findByJobId(jobId);

		if (jobInfo == null) {
			throw new RuntimeException("任务未找到");
		}
		model.addAttribute("jobId", jobInfo.getJobId());
		model.addAttribute("jobDesc", jobInfo.getJobDesc());

		Long scriptId = jobInfo.getJobScriptId();
		if(scriptId != null) {
			JobScript jobScript = jobServiceReference.jobService.findByJobScriptId(scriptId);
			if(jobScript != null)
				model.addAttribute("jobScript", jobScript.getScript());
		}
		return prefix + "/jobCode";
	}

	@Log(title = "定时任务脚本", businessType = BusinessType.UPDATE)
	@RequiresPermissions(JobPermissions.JobInfo.CODE_EDIT)
	@RequestMapping("/code/save")
	@AjaxWrapper
	public void save(Model model, long id, String jobScript, String jobRemark) {
		// valid
		if (StringUtils.isEmpty(jobScript)) {
			throw BusinessException.build("脚本内容不能为空");
		}
		if (jobScript.length()<10) {
			throw BusinessException.build("脚本内容不能为空,长度至少为10");
		}
		if (jobRemark==null) {
			throw BusinessException.build("备注不能为空");
		}
		if (jobRemark.length()<4 || jobRemark.length()>100) {
			throw BusinessException.build("备注不能为空,长度至少为4，最大为100");
		}
		Job exists_jobInfo = jobServiceReference.jobService.findByJobId(id);
		if (exists_jobInfo == null) {
			throw BusinessException.build("任务不存在");
		}

		JobScript jobS = new JobScript();
		jobS.setJobId(exists_jobInfo.getJobId());
		jobS.setScript(jobScript);
		jobS.setRemark(jobRemark);
		jobS.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
		jobS.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

		jobServiceReference.jobService.addJobScript(jobS);

		// 脚本历史记录保留10次
		jobServiceReference.jobService.removeOldScript(exists_jobInfo.getJobId(), 10);
	}
	
}
