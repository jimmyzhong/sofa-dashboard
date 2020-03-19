package me.izhong.dashboard.common.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserDeleteException extends BusinessException {


    public static final int KEY = 400;

    public UserDeleteException() {
        super(KEY, "用户已删除");
    }

    public UserDeleteException(String message) {
        super(KEY, message);
    }
}
