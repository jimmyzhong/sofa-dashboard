package me.izhong.dashboard.common.expection.user;

public class UserNameIsNullException extends UserException {


    public static final int KEY = 400;

    public UserNameIsNullException() {
        super(KEY, "用户名为空");
    }

    public UserNameIsNullException(String message) {
        super(KEY, message);
    }
}
