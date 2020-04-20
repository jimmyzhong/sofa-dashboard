package me.izhong.dashboard.web.controller.admin;

import me.izhong.common.domain.PageRequest;
import me.izhong.common.util.Convert;
import me.izhong.dashboard.manage.entity.SysDept;
import me.izhong.dashboard.manage.service.SysDeptService;
import me.izhong.db.mongo.util.CriteriaUtil;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.dashboard.common.annotation.Log;
import me.izhong.dashboard.common.constants.BusinessType;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.entity.SysUserRole;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.service.SysRoleService;
import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.common.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    @Autowired
    private SysDeptService sysDeptService;

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
        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
        if(!UserInfoContextHelper.getLoginUser().isHasAllDeptPerm())
            pageRequest.setDepts(UserInfoContextHelper.getLoginUser().getScopeData(PermissionConstants.Role.VIEW));
        PageModel<SysRole> list = sysRoleService.selectPage(pageRequest, sysRole);
        return list;
    }

    /**
     * 用户菜单，选择权限列表页面
     * 需要有 1.角色查询权限，2.用户授权权限 PermissionConstants.User.ROLE
     * @param sysRole
     * @param request
     * @return
     */
    @RequiresPermissions(PermissionConstants.Role.VIEW)
    @PostMapping("/list/forAuthUser")
    @AjaxWrapper
    public PageModel<SysRole> listRoles(SysRole sysRole, HttpServletRequest request) {
        sysRole.setIsDelete(false);
        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
//        if(!UserInfoContextHelper.getLoginUser().isHasAllDeptPerm())
//            pageRequest.setDepts(UserInfoContextHelper.getLoginUser().getScopeData(PermissionConstants.User.ROLE));

        Query query = new Query();
        Long currentUserId = UserInfoContextHelper.getCurrentUserId();
        SysUser curUser = sysUserService.findUser(currentUserId);
        SysDept curDept = sysDeptService.selectDeptByDeptId(curUser.getDeptId());


        Criteria visCri = new Criteria().orOperator(
                Criteria.where("visibleScope").is("1"),
                new Criteria().andOperator(
                        Criteria.where("visibleScope").is("2"),
                        Criteria.where("deptId").is(curDept.getDeptId())
                ),
                new Criteria().andOperator(
                        Criteria.where("visibleScope").is("3"),
                        Criteria.where("deptId").in(curDept.getDescendents())
                )
        );

        CriteriaUtil.addCriteria(query, visCri);

        PageModel<SysRole> list = sysRoleService.selectPage(query,pageRequest, sysRole);

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

        if (sysRole.getDeptId() != null) {
            SysDept sysDept = sysDeptService.selectDeptByDeptId(sysRole.getDeptId());
            if(sysDept == null)
                throw BusinessException.build("新增角色'" + sysRole.getRoleName() + "'失败，部门不存在");
            sysRole.setDeptId(sysRole.getDeptId());
            sysRole.setDeptName(sysDept.getDeptName());
        } else {
            throw BusinessException.build("新增角色'" + sysRole.getRoleName() + "'失败，部门不能为空");
        }

        UserInfoContextHelper.getLoginUser().checkScopePermission(PermissionConstants.Role.ADD,sysRole.getDeptId());

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
        if(dbSysRole.getDeptId() != null)
            UserInfoContextHelper.getLoginUser().checkScopePermission(PermissionConstants.Role.EDIT,dbSysRole.getDeptId());
        dbSysRole.setStatus(sysRole.getStatus());
        dbSysRole.setRoleName(sysRole.getRoleName());
        dbSysRole.setRoleKey(sysRole.getRoleKey());
        dbSysRole.setRoleSort(sysRole.getRoleSort());
        dbSysRole.setVisibleScope(sysRole.getVisibleScope());
        dbSysRole.setRemark(sysRole.getRemark());
        dbSysRole.setMenuIds(sysRole.getMenuIds());
        if (sysRole.getDeptId() != null) {
            SysDept sysDept = sysDeptService.selectDeptByDeptId(sysRole.getDeptId());
            if(sysDept == null)
                throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，部门不存在");
            dbSysRole.setDeptId(sysRole.getDeptId());
            dbSysRole.setDeptName(sysDept.getDeptName());
        } else {
            throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，部门不能为空");
        }

        if (!sysRoleService.checkRoleNameUnique(sysRole)) {
            throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!sysRoleService.checkRoleKeyUnique(sysRole)) {
            throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，角色权限已存在");
        }
        UserInfoContextHelper.getLoginUser().checkScopePermission(PermissionConstants.Role.EDIT,sysRole.getDeptId());
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
        Long[] userIds = Convert.toLongArray(ids);
        List<SysRole> roles = sysRoleService.selectRolesByRoleIds(userIds);
        if(roles != null ){
            roles.forEach(e ->
                    UserInfoContextHelper.checkScopePermission(PermissionConstants.Role.REMOVE,e.getDeptId())
            );
        }
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

}
