package me.izhong.dashboard.web.controller.admin;

import me.izhong.dashboard.manage.security.UserRealm;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.model.UserInfo;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.dashboard.manage.util.FileUploadUtil;
import me.izhong.dashboard.manage.security.service.PasswordService;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.util.ServletUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/system/user/profile")
public class SysProfileController {
    private static final Logger log = LoggerFactory.getLogger(SysProfileController.class);

    private String prefix = "system/user/profile";

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordService passwordService;

    /**
     * 个人信息
     */
    @GetMapping
    @RequiresAuthentication
    public String profile(ModelMap mmap) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        mmap.put("user", user);
        //角色  管理员
        mmap.put("roleGroup", sysUserService.selectUserRoleGroup(user.getUserId()));
        //岗位 董事长，老板
        mmap.put("postGroup", sysUserService.selectUserPostGroup(user.getUserId()));
        return prefix + "/profile";
    }

    @RequiresAuthentication
    @GetMapping("/checkPassword")
    @ResponseBody
    public boolean checkPassword(String password) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        SysUser dbUser = sysUserService.findUser(user.getUserId());

        if (passwordService.matches(dbUser, password)) {
            return true;
        }
        return false;
    }

    @GetMapping("/resetPwd")
    @RequiresAuthentication
    public String resetPwd(ModelMap mmap) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        mmap.put("user", sysUserService.findUser(user.getUserId()));
        return prefix + "/resetPwd";
    }

    @Log(title = "修改密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    @AjaxWrapper
    @RequiresAuthentication
    public String resetPwd(String oldPassword, String newPassword) {
        UserInfo loginUser = UserInfoContextHelper.getLoginUser();
        SysUser user = sysUserService.findUser(loginUser.getUserId());
        if (StringUtils.isEmpty(newPassword)) {
            throw BusinessException.build("修改密码失败，新密码不能为空");
        }

        if (passwordService.matches(user, oldPassword)) {
            String newPass = passwordService.encryptPassword(newPassword, Global.getSalt());
            sysUserService.resetUserPwd(user.getUserId(), newPass,Global.getSalt());
            return "修改密码成功";
        } else {
            throw BusinessException.build("修改密码失败，旧密码输入错误");
        }
    }

    /**
     * 修改用户
     */
    @RequiresAuthentication
    @GetMapping("/edit")
    public String edit(ModelMap mmap) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        mmap.put("user", sysUserService.findUser(user.getUserId()));
        return prefix + "/edit";
    }

    /**
     * 修改头像页面
     */
    @GetMapping("/avatar")
    @RequiresAuthentication
    public String avatar(ModelMap mmap) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        mmap.put("user", user);
        return prefix + "/avatar";
    }

    /**
     * 修改用户
     */
    @RequiresAuthentication
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    @AjaxWrapper
    public void update(String userName, String phoneNumber, String email, String sex) {
        UserInfo loginUser = UserInfoContextHelper.getSubjectUser();
        sysUserService.updateMyInfos(loginUser.getUserId(),userName,email,phoneNumber,sex);

        loginUser.setUserName(userName);
        loginUser.setEmail(email);
        loginUser.setPhoneNumber(phoneNumber);
        loginUser.setSex(sex);
        UserRealm.refreshUserScope();
    }

    /**
     * 保存头像
     */
    @RequiresAuthentication
    @Log(title = "个人头像", businessType = BusinessType.UPDATE)
    @PostMapping("/updateAvatar")
    @AjaxWrapper
    public void updateAvatar(@RequestParam("avatarfile") MultipartFile file) {
        UserInfo loginUser = UserInfoContextHelper.getLoginUser();
        try {
            if (!file.isEmpty()) {
                String avatar = FileUploadUtil.upload(Global.getAvatarPath(), file);
                sysUserService.updateMyAvatar(loginUser.getUserId(),avatar);

                String conextpath = ServletUtil.getRequest().getContextPath();
                String avatorUrl = Global.getAvatarMapping();
                loginUser.setAvatar(avatorUrl + avatar);
                UserInfoContextHelper.setUser(loginUser);
            } else {
                throw BusinessException.build("头像为空");
            }
        } catch (Exception e) {
            log.error("修改头像失败！", e);
            throw BusinessException.build(e.getMessage());
        }
    }
}
