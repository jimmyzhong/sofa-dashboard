package me.izhong.dashboard.manage.expection.user;

import me.izhong.common.exception.BusinessException;

public class UserDeleteException extends BusinessException {


    public static final String KEY = "USER_DELETE";

    public UserDeleteException() {
        super(KEY, "用户已删除");
    }

    public UserDeleteException(String message) {
        super(KEY, message);
    }
}
