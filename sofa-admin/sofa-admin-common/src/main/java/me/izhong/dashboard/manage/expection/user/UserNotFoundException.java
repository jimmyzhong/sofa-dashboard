package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {


    public static final String KEY = "USER_NOT_EXIST";

    public UserNotFoundException() {
        super(KEY, "用户不存在");
    }

    public UserNotFoundException(String message) {
        super(KEY, message);
    }
}
