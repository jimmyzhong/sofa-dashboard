package me.izhong.dashboard.common.expection;

import me.izhong.common.exception.BusinessException;

public class RoleBlockedException extends BusinessException {

    public static final int KEY = 400;

    public RoleBlockedException() {
        super(KEY, "角色已禁用");
    }

    public RoleBlockedException(String message) {
        super(KEY, message);
    }
}
