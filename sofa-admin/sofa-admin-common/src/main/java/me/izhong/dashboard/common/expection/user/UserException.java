package me.izhong.dashboard.common.expection.user;

import lombok.Getter;
import lombok.Setter;
import me.izhong.common.exception.BusinessException;

public class UserException extends BusinessException {

    public static final int KEY = 400;

    @Getter
    @Setter
    private String loginName;

    public UserException(String message) {
        this(KEY, message);
    }

    public UserException(int code, String message) {
        super(code, message);
    }

    public UserException(int code, String message, String loginName) {
        super(code, message);
        this.loginName = loginName;
    }
}
