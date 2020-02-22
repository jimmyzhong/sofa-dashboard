package me.izhong.dashboard.manage.util;

import me.izhong.common.model.UserInfo;
import me.izhong.dashboard.manage.entity.SysUser;

public class UserConvertUtil {
    public static UserInfo convert(SysUser sysUser) {
        UserInfo u = new UserInfo();
            if (sysUser != null) {
                u.setUserId(sysUser.getUserId());
                u.setLoginName(sysUser.getLoginName());
                u.setLoginIp(sysUser.getLoginIp());
                u.setUserName(sysUser.getUserName());
                u.setEmail(sysUser.getEmail());
                u.setPhoneNumber(sysUser.getPhoneNumber());
                u.setSex(sysUser.getSex());
                u.setAvatar(sysUser.getAvatar());
                u.setDeptId(sysUser.getDeptId());
                u.setCreateTime(sysUser.getCreateTime());
                u.setUpdateTime(sysUser.getUpdateTime());
                u.setDeptName(sysUser.getDeptName());
                u.setStatus(sysUser.getStatus());
                u.setCreateBy(sysUser.getCreateBy());
                u.setUpdateBy(sysUser.getUpdateBy());
            }
        return u;
    }
}
