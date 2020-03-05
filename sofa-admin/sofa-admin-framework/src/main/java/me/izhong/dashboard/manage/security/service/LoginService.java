package me.izhong.dashboard.manage.security.service;

import me.izhong.dashboard.manage.constants.ShiroConstants;
import me.izhong.dashboard.manage.constants.SystemConstants;
import me.izhong.dashboard.manage.constants.UserConstants;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.expection.*;
import me.izhong.dashboard.manage.expection.user.UserBlockedException;
import me.izhong.dashboard.manage.expection.user.UserDeleteException;
import me.izhong.dashboard.manage.expection.user.UserNotFoundException;
import me.izhong.dashboard.manage.expection.user.UserPasswordNotMatchException;
import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.dashboard.manage.factory.AsyncManager;
import me.izhong.dashboard.manage.factory.AsyncFactory;
import me.izhong.dashboard.manage.util.MessageUtil;
import me.izhong.dashboard.manage.util.ServletUtil;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 登录校验方法
 */
@Component
public class LoginService {
    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     */
    public SysUser login(String username, String password) {
        // 验证码校验
        if (!StringUtils.isEmpty(ServletUtil.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA))) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("not.null")));
            throw new UserNotFoundException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
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
            throw new UserNotFoundException();
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
