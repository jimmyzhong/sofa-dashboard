package me.izhong.dashboard.common.expection.user;

public class UserBlockedException extends UserException {


    public static final int KEY = 400;

    public UserBlockedException() {
        super(KEY, "用户已禁用");
    }

    public UserBlockedException(String message) {
        super(KEY, message);
    }

    public UserBlockedException(String message,String loginName) {
        super(KEY, message, loginName);
    }
}
