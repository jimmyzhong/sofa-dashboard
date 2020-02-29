package me.izhong.jobs.admin.controller;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.jobs.admin.config.JobPermissions;
import me.izhong.jobs.admin.service.JobServiceReference;
import me.izhong.jobs.model.JobGroup;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.Convert;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/ext/djob/group")
public class JobGroupController {

	private String prefix = "ext/djob/group";

	@Autowired(required = false)
	private JobServiceReference jobServiceReference;

	@RequestMapping
	public String index(Model model) {
		return prefix + "/group";
	}

	@RequestMapping("/list")
	@AjaxWrapper
	public PageModel<JobGroup> pageList(HttpServletRequest request, JobGroup ino) {
		ino.setIsDelete(false);
		return jobServiceReference.jobService.selectJobGroupPage(PageRequestUtil.fromRequest(request),ino);
	}

	@GetMapping("/add")
	public String add() {
		return prefix + "/add";
	}

	@Log(title = "定时任务分组", businessType = BusinessType.ADD)
	@RequiresPermissions(JobPermissions.JobGroup.ADD)
	@PostMapping("/add")
	@AjaxWrapper
	public JobGroup addSave(JobGroup jg) throws BusinessException {
		if (jg.getGroupName()==null || jg.getGroupName().trim().length()==0) {
			throw BusinessException.build("名称不能为空");
		}
		if (jg.getGroupName().length()<4 || jg.getGroupName().length()>64) {
            throw BusinessException.build("名称长度应该在4-64之间");
		}

		jg.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
		jg.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
		return jobServiceReference.jobService.addJobGroup(jg);
	}

	@GetMapping("/edit/{groupId}")
	public String edit(@PathVariable("groupId") Long groupId, ModelMap mmap) {
		mmap.put("group", jobServiceReference.jobService.findJobGroup(groupId));
		return prefix + "/edit";
	}

	@Log(title = "定时任务分组", businessType = BusinessType.UPDATE)
	@RequiresPermissions(JobPermissions.JobGroup.EDIT)
	@PostMapping("/edit")
	@AjaxWrapper
	public JobGroup editSave(JobGroup JobGroup) throws BusinessException
	{
		if (JobGroup.getGroupName()==null || JobGroup.getGroupName().trim().length()==0) {
			throw BusinessException.build("GroupName不能为空");
		} else if (JobGroup.getGroupName().length()<4 || JobGroup.getGroupName().length()>64) {
			throw BusinessException.build("GroupName长度需要在4-64之间");
		}
		JobGroup.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
		return jobServiceReference.jobService.updateJobGroup(JobGroup);
	}

	@Log(title = "定时任务分组", businessType = BusinessType.DELETE)
	@RequiresPermissions(JobPermissions.JobGroup.REMOVE)
	@RequestMapping("/remove")
	@AjaxWrapper
	public Long remove(String ids){
		List<Long> idLongs = Convert.toLongList(ids);
		if(idLongs.size() < 1)
			throw BusinessException.build("删除的数量不能小于1");
		return jobServiceReference.jobService.removeJobGroup(idLongs);
	}

	@RequestMapping("/find")
	@AjaxWrapper
	public JobGroup loadById(long id){
		return jobServiceReference.jobService.findJobGroup(id);
	}

}
