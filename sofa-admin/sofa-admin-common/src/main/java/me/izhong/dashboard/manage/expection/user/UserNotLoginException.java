package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserNotLoginException extends BusinessException {


    public static final int KEY = 400;

    public UserNotLoginException() {
        super(KEY, "用户没有登录");
    }

    public UserNotLoginException(String message) {
        super(KEY, message);
    }
}
