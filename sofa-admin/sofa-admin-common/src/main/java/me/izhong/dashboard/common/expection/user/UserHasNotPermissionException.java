package me.izhong.dashboard.common.expection.user;

public class UserHasNotPermissionException extends UserException {

    public static final int KEY = 400;

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
