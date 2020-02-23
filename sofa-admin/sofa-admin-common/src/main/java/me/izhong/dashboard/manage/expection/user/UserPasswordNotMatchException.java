package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserPasswordNotMatchException extends BusinessException {


    public static final int KEY = 400;

    public UserPasswordNotMatchException() {
        super(KEY, "密码不正确");
    }

    public UserPasswordNotMatchException(String message) {
        super(KEY, message);
    }
}
