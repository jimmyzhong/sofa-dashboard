package me.izhong.dashboard.web.controller.admin;

import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.entity.SysUserRole;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.service.SysRoleService;
import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/system/role")
public class RoleAdminController {
    private String prefix = "system/role";

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysUserService sysUserService;

    @RequiresPermissions(PermissionConstants.Role.VIEW)
    @GetMapping()
    public String role() {
        return prefix + "/role";
    }

    @RequiresPermissions(PermissionConstants.Role.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<SysRole> list(SysRole sysRole, HttpServletRequest request) {
        sysRole.setIsDelete(false);
        PageModel<SysRole> list = sysRoleService.selectPage(PageRequestUtil.fromRequest(request), sysRole);
        return list;
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.Role.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(SysRole sysRole, HttpServletRequest request) {
        List<SysRole> list = sysRoleService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysRole);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        return util.exportExcel(list, "角色数据");
    }

    /**
     * 新增角色
     */
    @RequiresPermissions(PermissionConstants.Role.ADD)
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存角色
     */
    @RequiresPermissions(PermissionConstants.Role.VIEW)
    @Log(title = "角色管理", businessType = BusinessType.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public int addSave(SysRole sysRole) {
        if (!sysRoleService.checkRoleNameUnique(sysRole)) {
            throw BusinessException.build("新增角色'" + sysRole.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!sysRoleService.checkRoleKeyUnique(sysRole)) {
            throw BusinessException.build("新增角色'" + sysRole.getRoleName() + "'失败，角色权限已存在");
        }
        if(sysRole.getDataScope() == null) {
            sysRole.setDataScope("1"); //所有数据权限
        }
        sysRole.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        sysRole.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return sysRoleService.insertRole(sysRole);
    }


    @RequiresPermissions(PermissionConstants.Role.EDIT)
    @GetMapping("/edit/{roleId}")
    public String edit(@PathVariable("roleId") Long roleId, ModelMap mmap) {
        mmap.put("role", sysRoleService.selectByPId(roleId));
        return prefix + "/edit";
    }

    @RequiresPermissions(PermissionConstants.Role.EDIT)
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @AjaxWrapper
    public int editSave(SysRole sysRole) {
        SysRole dbSysRole = sysRoleService.selectByPId(sysRole.getRoleId());
        dbSysRole.setStatus(sysRole.getStatus());
        dbSysRole.setRoleName(sysRole.getRoleName());
        dbSysRole.setRoleKey(sysRole.getRoleKey());
        dbSysRole.setRoleSort(sysRole.getRoleSort());
        dbSysRole.setRemark(sysRole.getRemark());
        dbSysRole.setMenuIds(sysRole.getMenuIds());

        if (!sysRoleService.checkRoleNameUnique(sysRole)) {
            throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!sysRoleService.checkRoleKeyUnique(sysRole)) {
            throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，角色权限已存在");
        }
        dbSysRole.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return sysRoleService.updateRole(dbSysRole);
    }

    /**
     * 角色分配数据权限
     */
    @GetMapping("/authDataScope/{roleId}")
    public String authDataScope(@PathVariable("roleId") Long roleId, ModelMap mmap) {
        mmap.put("role", sysRoleService.selectByPId(roleId));
        return prefix + "/dataScope";
    }

    /**
     * 保存角色分配数据权限
     */
    @RequiresPermissions(PermissionConstants.Role.EDIT)
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/authDataScope")
    @AjaxWrapper
    public int authDataScopeSave(SysRole sysRole) {
        return sysRoleService.authDataScope(sysRole);
    }

    @RequiresPermissions(PermissionConstants.Role.REMOVE)
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) throws BusinessException {
        return sysRoleService.removeRoleInfo(ids);
    }

    @PostMapping("/checkRoleNameUnique")
    @ResponseBody
    public boolean checkRoleNameUnique(SysRole sysRole) {
        return sysRoleService.checkRoleNameUnique(sysRole);
    }


    @PostMapping("/checkRoleKeyUnique")
    @ResponseBody
    public boolean checkRoleKeyUnique(SysRole sysRole) {
        return sysRoleService.checkRoleKeyUnique(sysRole);
    }

    /**
     * 选择菜单树
     */
    @GetMapping("/selectMenuTree")
    public String selectMenuTree() {
        return prefix + "/tree";
    }

    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions(PermissionConstants.Role.EDIT)
    @PostMapping("/changeStatus")
    @AjaxWrapper
    public int changeStatus(SysRole sysRole) {
        sysRoleService.checkRoleAllowed(sysRole);
        return sysRoleService.changeStatus(sysRole);
    }


    @RequiresPermissions(PermissionConstants.Role.EDIT)
    @GetMapping("/authUser/{roleId}")
    public String authUser(@PathVariable("roleId") Long roleId, ModelMap mmap) {
        mmap.put("role", sysRoleService.selectByPId(roleId));
        return prefix + "/authUser";
    }

    /**
     * 取消授权
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/cancel")
    @AjaxWrapper
    public long cancelAuthUser(SysUserRole sysUserRole) {
        return sysRoleService.deleteAuthUser(sysUserRole);
    }

    /**
     * 批量取消授权
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/cancelAll")
    @AjaxWrapper
    public long cancelAuthUserAll(Long roleId, String userIds) {
        return sysRoleService.deleteAuthUsers(roleId, userIds);
    }

    /**
     * 选择用户
     */
    @GetMapping("/authUser/selectUser/{roleId}")
    public String selectUser(@PathVariable("roleId") Long roleId, ModelMap mmap) {
        mmap.put("role", sysRoleService.selectByPId(roleId));
        return prefix + "/selectUser";
    }

    /**
     * 查询已分配用户角色列表
     */
    @RequiresPermissions(PermissionConstants.Role.VIEW)
    @PostMapping("/authUser/allocatedList")
    @AjaxWrapper
    public PageModel<SysUser> allocatedList(Long roleId, SysUser user, HttpServletRequest request) {
        return sysUserService.selectAllocatedList(PageRequestUtil.fromRequest(request), roleId, user, null);
    }

    /**
     * 查询未分配用户角色列表
     */
    @RequiresPermissions(PermissionConstants.Role.VIEW)
    @PostMapping("/authUser/unallocatedList")
    @AjaxWrapper
    public PageModel<SysUser> unallocatedList(Long roleId, SysUser user, HttpServletRequest request) {
        return sysUserService.selectUnallocatedList(PageRequestUtil.fromRequest(request), roleId, user, null);
    }

    /**
     * 批量选择用户授权
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/selectAll")
    @AjaxWrapper
    public int selectAuthUserAll(Long roleId, String userIds) {
        return sysRoleService.insertAuthUsers(roleId, userIds);
    }

    private void sortRoles(List<SysRole> sysRoles) {
        if (sysRoles == null || sysRoles.size() == 0)
            return;

        Collections.sort(sysRoles, (e1, e2) -> {
            return e1.getRoleSort().compareTo(e2.getRoleSort());
        });
    }
}
