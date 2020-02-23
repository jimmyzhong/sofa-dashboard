package me.izhong.dashboard.web.controller.admin;

import me.izhong.common.util.TimeUtil;
import me.izhong.dashboard.manage.service.*;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.entity.SysDept;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.security.service.PasswordService;
import me.izhong.common.util.Convert;
import me.izhong.dashboard.manage.util.ExcelUtil;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.util.UserConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/system/user")
public class UserAdminController {

    private String prefix = "system/user";

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPostService sysPostService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysConfigService sysConfigService;

    @RequiresPermissions(PermissionConstants.User.VIEW)
    @RequestMapping
    public String user() {
        return prefix + "/user";
    }

    @RequiresPermissions(PermissionConstants.User.VIEW)
    //@PreAuthorize("hasAnyAuthority('"+ PermissionConstants.User.VIEW + "')")
    @RequestMapping("list")
    @AjaxWrapper
    public PageModel<UserInfo> list(@RequestParam(value = "loginName", required = false) String loginName,
                                    @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                    @RequestParam(value = "deptId", required = false) Long deptId,
                                    HttpServletRequest request) {
        SysUser searchUser = new SysUser();
        searchUser.setPhoneNumber(phoneNumber);
        searchUser.setLoginName(loginName);
        searchUser.setDeptId(deptId);
        searchUser.setIsDelete(false);

        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
        if(!UserInfoContextHelper.getLoginUser().isHasAllDeptPerm())
            pageRequest.setDepts(UserInfoContextHelper.getLoginUser().getScopeData(PermissionConstants.User.VIEW));

        PageModel<SysUser> pe = sysUserService.getPage(pageRequest, searchUser);
        List<SysUser> lue = pe.getRows();
        if(pe == null)
            return null;
        List<UserInfo> uinfs = lue.stream().map(e -> UserConvertUtil.convert(e)).collect(Collectors.toList());
        return PageModel.instance(pe.getCount(),uinfs);
    }

    @RequiresPermissions(PermissionConstants.User.ADD)
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("roles", sysRoleService.selectAll());
        mmap.put("posts", sysPostService.selectAll());
        return prefix + "/add";
    }

    @RequiresPermissions(PermissionConstants.User.ADD)
    @Log(title = "用户管理", businessType = BusinessType.ADD)
    @PostMapping("add")
    @AjaxWrapper
    public UserInfo addUser(@Validated SysUser user) {

        UserInfoContextHelper.getLoginUser().checkScopePermission(PermissionConstants.User.ADD,user.getDeptId());

        SysUser dbUser = new SysUser();
        if (StringUtils.isBlank(user.getLoginName())) {
            throw BusinessException.build("loginName不能为空");
        }
        if (StringUtils.isBlank(user.getLoginName())) {
            throw BusinessException.build("登录名不能为空");
        }
        if (!sysUserService.checkLoginNameUnique(user)) {
            throw BusinessException.build("用登录名字已经存在");
        }
        if (!sysUserService.checkPhoneUnique(user)) {
            throw BusinessException.build("手机号已经存在");
        }
        if (StringUtils.isBlank(user.getPassword())) {
            throw BusinessException.build("密码不能为空");
        } else {
            dbUser.setSalt(Global.getSalt());
            dbUser.setPassword(passwordService.encryptPassword(user.getPassword(), Global.getSalt()));
            dbUser.setPasswordUpdateTime(new Date());
        }

        dbUser.setStatus(user.getStatus());
        dbUser.setPhoneNumber(user.getPhoneNumber());
        dbUser.setEmail(user.getEmail());
        dbUser.setUserName(user.getUserName());
        dbUser.setLoginName(user.getLoginName());
        dbUser.setSex(user.getSex());
        dbUser.setPostIds(user.getPostIds());
        if (user.getDeptId() != null) {
            SysDept sysDept = sysDeptService.selectDeptByDeptId(user.getDeptId());
            dbUser.setDeptId(user.getDeptId());
            dbUser.setDeptName(sysDept.getDeptName());
        }
        dbUser.setRoleIds(user.getRoleIds());
        dbUser.setRemark(user.getRemark());

        dbUser.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return UserConvertUtil.convert(sysUserService.saveUserAndPerms(dbUser));
    }

    @RequiresPermissions(PermissionConstants.User.EDIT)
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, ModelMap mmap) {
        mmap.put("user", sysUserService.findUser(userId));
        mmap.put("roles", sysRoleService.selectAllRolesByUserId(userId));
        mmap.put("posts", sysPostService.selectPostsByUserId(userId));
        return prefix + "/edit";
    }

    @RequiresPermissions(PermissionConstants.User.EDIT)
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("edit")
    @AjaxWrapper
    public UserInfo editUser(SysUser user) {
        sysUserService.checkUserAllowed(user);

        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,user.getDeptId());

        SysUser dbUser = null;
        if (user == null) {
            throw BusinessException.build("user不能为空");
        }
        if (user.getUserId() == null) {
            throw BusinessException.build("用户ID不能为空");
        }
        //修改
        dbUser = sysUserService.findUser(user.getUserId());

        if(dbUser.getDeptId() != null)
            UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,dbUser.getDeptId());

        if (StringUtils.isBlank(user.getLoginName())) {
            throw BusinessException.build("loginName不能为空");
        }
        if (!sysUserService.checkLoginNameUnique(user)) {
            throw BusinessException.build("用户名字已经存在");
        }
        if (!sysUserService.checkPhoneUnique(user)) {
            throw BusinessException.build("手机号已经存在");
        }
//        修改没有密码输入框，需要使用密码重置按钮
//        if (StringUtils.isNotBlank(user.getPassword())) {
//            String enPass = passwordService.encryptPassword(user.getPassword(),dbUser.getSalt());
//            if(!StringUtils.equals(enPass,dbUser.getPassword())) {
//                dbUser.setPassword(enPass);
//                dbUser.setPasswordUpdateTime(new Date());
//            }
//        }

        dbUser.setStatus(user.getStatus());
        dbUser.setAvatar(user.getAvatar());
        dbUser.setPhoneNumber(user.getPhoneNumber());
        dbUser.setEmail(user.getEmail());
        dbUser.setUserName(user.getUserName());
        dbUser.setSex(user.getSex());
        dbUser.setPostIds(user.getPostIds());
        if (user.getDeptId() != null) {
            dbUser.setDeptId(user.getDeptId());
            SysDept sysDept = sysDeptService.selectDeptByDeptId(user.getDeptId());
            dbUser.setDeptName(sysDept.getDeptName());
        }
        dbUser.setRoleIds(user.getRoleIds());
        dbUser.setRemark(user.getRemark());
        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return UserConvertUtil.convert(sysUserService.saveUserAndPerms(dbUser));
    }

    @RequiresPermissions(PermissionConstants.User.VIEW)
    @RequestMapping("count")
    public long count(@RequestParam(value = "loginName", required = false) String username,
                      @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                      @RequestParam(value = "deptId", required = false) Long deptId,
                      @RequestParam(value = "beginTime", required = false) String beginTime,
                      @RequestParam(value = "endTime", required = false) String endTime) {
        SysUser searchUser = new SysUser();
        searchUser.setPhoneNumber(phoneNumber);
        searchUser.setLoginName(username);
        searchUser.setDeptId(deptId);
        Date bTime = TimeUtil.parseDate_yyyyMMdd_hl(beginTime);
        Date eTime = TimeUtil.parseDate_yyyyMMdd_hl(endTime);

        return sysUserService.getListSize(searchUser, bTime, eTime);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.User.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(HttpServletRequest request, SysUser user) {
        PageModel<SysUser> list = sysUserService.getPage(PageRequestUtil.fromRequestIgnorePageSize(request), user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.exportExcel(list.getRows(), "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions(PermissionConstants.User.IMPORT)
    @PostMapping("/importData")
    @AjaxWrapper
    public String importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = UserInfoContextHelper.getCurrentLoginName();
        String message = sysUserService.importUser(userList, updateSupport, operName);
        return message;
    }

    @RequiresPermissions(PermissionConstants.User.VIEW)
    @GetMapping("/importTemplate")
    @AjaxWrapper
    public String importTemplate() {
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }


    @RequiresPermissions(PermissionConstants.User.REMOVE)
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        Long[] userIds = Convert.toLongArray(ids);
        List<SysUser> users = sysUserService.findUsersByUserIds(userIds);
        if(users != null ){
            users.forEach(e ->
                    UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,e.getDeptId())
            );
        }
        return sysUserService.deleteUserByIds(ids);
    }

    /**
     * 校验用户名
     */
    @PostMapping("/checkLoginNameUnique")
    @ResponseBody
    public boolean checkLoginNameUnique(SysUser user) {
        return sysUserService.checkLoginNameUnique(user);
    }

    /**
     * 校验手机号码
     */
    @PostMapping("/checkPhoneUnique")
    @ResponseBody
    public boolean checkPhoneUnique(SysUser user) {
        return sysUserService.checkPhoneUnique(user);
    }

    /**
     * 校验email邮箱
     */
    @PostMapping("/checkEmailUnique")
    @ResponseBody
    public boolean checkEmailUnique(SysUser user) {
        return sysUserService.checkEmailUnique(user);
    }

    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @RequiresPermissions(PermissionConstants.User.RESET_PWD)
    @GetMapping("/resetPwd/{userId}")
    public String resetPwd(@PathVariable("userId") Long userId, ModelMap mmap) {
        mmap.put("user", sysUserService.findUser(userId));
        return prefix + "/resetPwd";
    }

    @RequiresPermissions(PermissionConstants.User.RESET_PWD)
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    @AjaxWrapper
    public void resetPwd(SysUser user) {
        sysUserService.checkUserAllowed(user);

        SysUser dbUser = sysUserService.findUser(user.getUserId());
        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.RESET_PWD,dbUser.getDeptId());

        user.setSalt(Global.getSalt());
        user.setPassword(passwordService.encryptPassword(user.getPassword(), user.getSalt()));
        //解锁账户
        passwordService.unlock(dbUser.getUserName());

        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        sysUserService.resetUserPwd(user.getUserId(), user.getPassword(),user.getSalt());
    }

    /**
     * 用户状态修改
     */
    @RequiresPermissions(PermissionConstants.User.EDIT)
    @Log(title = "状态修改", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    @AjaxWrapper
    public int changeStatus(SysUser user) {
        sysUserService.checkUserAllowed(user);
        SysUser u = sysUserService.findUser(user.getUserId());
        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,u.getDeptId());
        //解锁账户
        passwordService.unlock(u.getUserName());
        u.setStatus(user.getStatus());
        sysUserService.saveUser(u);
        return 1;
    }

}
