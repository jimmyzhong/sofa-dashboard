package me.izhong.dashboard.manage.security.service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.common.constants.Global;
import me.izhong.dashboard.common.constants.ShiroConstants;
import me.izhong.dashboard.common.constants.SystemConstants;
import me.izhong.dashboard.common.constants.UserConstants;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.common.expection.*;
import me.izhong.dashboard.common.expection.user.UserBlockedException;
import me.izhong.dashboard.common.expection.user.UserDeleteException;
import me.izhong.dashboard.common.expection.user.UserNotFoundException;
import me.izhong.dashboard.common.expection.user.UserPasswordNotMatchException;
import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.dashboard.manage.factory.AsyncManager;
import me.izhong.dashboard.manage.factory.AsyncFactory;
import me.izhong.dashboard.common.util.MessageUtil;
import me.izhong.dashboard.common.util.ServletUtil;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 登录校验方法
 */
@Component
@Slf4j
public class LoginService {
    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     */
    public SysUser login(String username, String password) {

        if(Global.isDebugMode()) {
            String debugUserName = Global.getDebugLoginName();
            String debugPassword = Global.getDebugPassword();
            if(!org.apache.commons.lang3.StringUtils.equals(debugUserName,username)) {
                throw BusinessException.build("维护中，"+username+"不能登陆");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(debugPassword,password)) {
                throw BusinessException.build("维护中，"+username+"密码不正确，不能登陆");
            }
            SysUser user = sysUserService.findUserByLoginName(username);
            if (user == null) {
                throw BusinessException.build("虚拟" + username + "不存在");
            }
            log.info("DebugMode用户{}登陆成功",username);
            return user;
        }
        // 验证码校验
        if (!StringUtils.isEmpty(ServletUtil.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA))) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("not.null")));
            throw new UserNotFoundException(username);
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException("用户名长度不正确，长度应该在"+ UserConstants.USERNAME_MIN_LENGTH +"和" + UserConstants.USERNAME_MAX_LENGTH + "之间",username);
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException("密码长度不正确，长度应该在"+ UserConstants.PASSWORD_MIN_LENGTH +"和" + UserConstants.PASSWORD_MAX_LENGTH + "之间",username);
        }
        // 查询用户信息
        SysUser user = sysUserService.findUserByLoginName(username);

        if (user == null && maybeMobilePhoneNumber(username)) {
            user = sysUserService.findUserByPhoneNumber(username);
        }

        if (user == null && maybeEmail(username)) {
            user = sysUserService.findUserByEmail(username);
        }

        if (user == null) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.not.exists")));
            throw new UserNotFoundException(username);
        }

        if (Boolean.TRUE.equals(user.getIsDelete())) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.delete")));
            throw new UserDeleteException();
        }

        if (UserConstants.USER_DELETED.equals(user.getStatus())) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.blocked", user.getRemark())));
            throw new UserBlockedException();
        }

        passwordService.validate(user, password);

        AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_SUCCESS, MessageUtil.message("user.login.success")));
        user = sysUserService.recordLoginIp(user.getUserId(),UserInfoContextHelper.getIp());
        return user;
    }

    private boolean maybeEmail(String username) {
        if (!username.matches(UserConstants.EMAIL_PATTERN)) {
            return false;
        }
        return true;
    }

    private boolean maybeMobilePhoneNumber(String username) {
        if (!username.matches(UserConstants.MOBILE_PHONE_NUMBER_PATTERN)) {
            return false;
        }
        return true;
    }

}
