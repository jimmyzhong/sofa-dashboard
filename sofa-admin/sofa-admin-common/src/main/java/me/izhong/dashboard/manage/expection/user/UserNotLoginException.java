package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserNotLoginException extends BusinessException {


    public static final String KEY = "USER_NOT_LOGIN";

    public UserNotLoginException() {
        super(KEY, "用户没有登录");
    }

    public UserNotLoginException(String message) {
        super(KEY, message);
    }
}
