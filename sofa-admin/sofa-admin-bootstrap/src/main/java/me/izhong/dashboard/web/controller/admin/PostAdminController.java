package me.izhong.dashboard.web.controller.admin;

import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.entity.SysPost;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.service.SysPostService;
import me.izhong.dashboard.manage.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/system/post")
public class PostAdminController {
    private String prefix = "system/post";

    @Autowired
    private SysPostService sysPostService;

    @Autowired
    private SysUserService sysUserService;

    @RequiresPermissions(PermissionConstants.Post.VIEW)
    @GetMapping()
    public String operlog() {
        return prefix + "/post";
    }

    @RequiresPermissions(PermissionConstants.Post.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(SysPost sysPost, HttpServletRequest request) {
        sysPost.setIsDelete(false);
        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
        PageModel<SysPost> list = sysPostService.selectPage(pageRequest, sysPost);
        return list;
    }

    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:post:export")
    @PostMapping("/export")
    @ResponseBody
    public String export(SysPost sysPost, HttpServletRequest request) {
        PageRequest pageRequest = PageRequestUtil.fromRequestIgnorePageSize(request);
        PageModel<SysPost> list = sysPostService.selectPage(pageRequest, sysPost);
        ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
        return util.exportExcel(list.getRows(), "岗位数据");
    }

    @RequiresPermissions(PermissionConstants.Post.REMOVE)
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) throws BusinessException {
        return sysPostService.removePostInfo(ids);
    }


    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }


    @RequiresPermissions(PermissionConstants.Post.VIEW)
    @Log(title = "岗位管理", businessType = BusinessType.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public SysPost addSave(@Validated SysPost sysPost) {
        if (!sysPostService.checkPostNameUnique(sysPost)) {
            throw BusinessException.build("新增岗位'" + sysPost.getPostName() + "'失败，岗位名称已存在");
        }
        else if (!sysPostService.checkPostCodeUnique(sysPost)) {
            throw BusinessException.build("新增岗位'" + sysPost.getPostName() + "'失败，岗位编码已存在");
        }
        return sysPostService.insert(sysPost);
    }


    @RequiresPermissions(PermissionConstants.Post.EDIT)
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @GetMapping("/edit/{postId}")
    public String edit(@PathVariable("postId") Long postId, ModelMap mmap) {
        mmap.put("post", sysPostService.selectByPId(postId));
        return prefix + "/edit";
    }


    @RequiresPermissions(PermissionConstants.Post.EDIT)
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysPost editSave(@Validated SysPost sysPost) {
        if (!sysPostService.checkPostNameUnique(sysPost)) {
            throw BusinessException.build("修改岗位'" + sysPost.getPostName() + "'失败，岗位名称已存在");
        }
        else if (!sysPostService.checkPostCodeUnique(sysPost)) {
            throw BusinessException.build("修改岗位'" + sysPost.getPostName() + "'失败，岗位编码已存在");
        }
        return sysPostService.update(sysPost);
    }


    @PostMapping("/checkPostNameUnique")
    @ResponseBody
    public boolean checkPostNameUnique(SysPost sysPost) {
        return sysPostService.checkPostNameUnique(sysPost);
    }

    @PostMapping("/checkPostCodeUnique")
    @ResponseBody
    public boolean checkPostCodeUnique(SysPost sysPost) {
        return sysPostService.checkPostCodeUnique(sysPost);
    }
}
