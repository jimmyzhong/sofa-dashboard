package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserPasswordRetryLimitExceedException extends BusinessException {


    public static final int KEY = 400;

    public UserPasswordRetryLimitExceedException() {
        super(KEY, "密码输入错误次数超过限制，请联系管理员");
    }

    public UserPasswordRetryLimitExceedException(String message) {
        super(KEY, message);
    }

    public UserPasswordRetryLimitExceedException(long count) {
        super(KEY, "密码输入错误次数超过" + count +"次，请联系管理员");
    }
}
