package me.izhong.dashboard.common.expection.user;

public class UserNotLoginException extends UserException {


    public static final int KEY = 400;

    public UserNotLoginException() {
        super(KEY, "用户没有登录");
    }

    public UserNotLoginException(String message) {
        super(KEY, message);
    }

    public UserNotLoginException(String message, String loginName) {
        super(KEY, message, loginName);
    }
}
