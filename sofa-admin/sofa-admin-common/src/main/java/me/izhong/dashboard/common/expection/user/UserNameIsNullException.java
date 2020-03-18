package me.izhong.dashboard.common.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserNameIsNullException extends BusinessException {


    public static final int KEY = 400;

    public UserNameIsNullException() {
        super(KEY, "用户名为空");
    }

    public UserNameIsNullException(String message) {
        super(KEY, message);
    }
}
