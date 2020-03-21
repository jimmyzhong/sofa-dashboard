package me.izhong.dashboard.common.expection.user;

public class UserNotFoundException extends UserException {


    public static final int KEY = 400;

    public UserNotFoundException() {
        super(KEY, "用户不存在");
    }

    public UserNotFoundException(String loginName) {
        super(KEY, "用户不存在", loginName);
    }
}
