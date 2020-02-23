package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserBlockedException extends BusinessException {


    public static final int KEY = 400;

    public UserBlockedException() {
        super(KEY, "用户已禁用");
    }

    public UserBlockedException(String message) {
        super(KEY, message);
    }
}
