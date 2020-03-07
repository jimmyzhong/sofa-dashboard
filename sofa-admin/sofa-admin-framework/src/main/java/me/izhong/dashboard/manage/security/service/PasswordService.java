package me.izhong.dashboard.manage.security.service;

import me.izhong.dashboard.manage.constants.ShiroConstants;
import me.izhong.dashboard.manage.constants.SystemConstants;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.expection.user.UserPasswordNotMatchException;
import me.izhong.dashboard.manage.expection.user.UserPasswordRetryLimitExceedException;
import me.izhong.dashboard.manage.factory.AsyncManager;
import me.izhong.dashboard.manage.factory.AsyncFactory;
import me.izhong.dashboard.manage.util.MD5Util;
import me.izhong.dashboard.manage.util.MessageUtil;
import me.izhong.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PasswordService {

    @Autowired(required = false)
    private CacheManager cacheManager;

    private Cache<String, AtomicInteger> loginRecordCache;

    @Value(value = "${user.password.maxRetryCount:3}")
    private String maxRetryCount;

    @PostConstruct
    public void init() {
        if(cacheManager != null)
            loginRecordCache = cacheManager.getCache(ShiroConstants.LOGINRECORDCACHE);
    }


    public void validate(SysUser user, String password) {
        String loginName = user.getLoginName();
        AtomicInteger retryCount = new AtomicInteger(0);
        //AtomicInteger retryCount = loginRecordCache.get(loginName);

//        if (retryCount == null) {
//            retryCount = new AtomicInteger(0);
//            loginRecordCache.put(loginName, retryCount);
//        }
        if (retryCount.incrementAndGet() > Integer.valueOf(maxRetryCount).intValue()) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(loginName, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.retry.limit.exceed", maxRetryCount)));
            throw new UserPasswordRetryLimitExceedException(Integer.valueOf(maxRetryCount).intValue());
        }

        if (!matches(user, password)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(loginName, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.retry.limit.count", retryCount)));
          //  loginRecordCache.put(loginName, retryCount);
            throw new UserPasswordNotMatchException();
        } else {
            clearLoginRecordCache(loginName);
        }
    }



    public boolean matches(SysUser user, String rawPassword) {
        return user.getPassword().equals(encryptPassword(rawPassword, user.getSalt()));
    }

    public String encryptPassword(String password, String salt) {
        if(StringUtils.isBlank(password)) {
            throw BusinessException.build("密码异常，不能为空");
        }
        if(StringUtils.isBlank(salt)) {
            throw BusinessException.build("密码异常，随机因子不能为空");
        }
        return MD5Util.hash(password + salt);
    }

    public void clearLoginRecordCache(String username) {
        //loginRecordCache.remove(username);
    }


    public void unlock(String loginName){
        //loginRecordCache.remove(loginName);
    }
}
