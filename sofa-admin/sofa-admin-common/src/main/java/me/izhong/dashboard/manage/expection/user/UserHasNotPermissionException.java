package me.izhong.dashboard.manage.expection.user;

import me.izhong.db.common.exception.BusinessException;

public class UserHasNotPermissionException extends BusinessException {

    public static final String KEY = "USER_NOT_PERMIT";

    private String permission;

    public UserHasNotPermissionException() {
        super(KEY, "用户没有权限");
    }

    public UserHasNotPermissionException(String message) {
        super(KEY, message);
    }

    public static UserHasNotPermissionException buildWithPermission(String permission) {
        UserHasNotPermissionException e = new UserHasNotPermissionException();
        e.setPermission(permission);
        return e;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
