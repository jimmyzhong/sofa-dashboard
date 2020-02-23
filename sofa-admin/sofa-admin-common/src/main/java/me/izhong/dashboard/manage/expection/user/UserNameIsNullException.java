package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserNameIsNullException extends BusinessException {


    public static final String KEY = "USER_NAME_IS_NULL";

    public UserNameIsNullException() {
        super(KEY, "用户名为空");
    }

    public UserNameIsNullException(String message) {
        super(KEY, message);
    }
}
