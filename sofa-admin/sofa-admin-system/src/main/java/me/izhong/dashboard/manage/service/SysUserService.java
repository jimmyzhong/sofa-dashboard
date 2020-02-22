package me.izhong.dashboard.manage.service;


import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.dashboard.manage.entity.SysUser;

import java.util.Date;
import java.util.List;

public interface SysUserService {

    SysUser login(String username, String password);

    SysUser findUser(Long id);

    SysUser findUserByLoginName(String username);

    SysUser findUserByEmail(String email);

    SysUser findUserByPhoneNumber(String phoneNumber);

    SysUser saveUser(SysUser user);

    SysUser saveUserAndPerms(SysUser user);

    List<SysUser> getTop(int size, String order, SysUser searchUser);

    List<SysUser> getList(int pageNum, int pageSize, String order, String isAsc, SysUser searchUser, Date bTime, Date eTime);

    List<SysUser> findUsersByUserIds(Long[] id);

    PageModel getPage(PageRequest pageRequest, SysUser searchUser);

    long getListSize(SysUser searchUser, Date bTime, Date eTime);

    boolean checkPhoneUnique(SysUser user);

    boolean checkEmailUnique(SysUser user);

    boolean checkLoginNameUnique(SysUser user);

    PageModel<SysUser> selectUnallocatedList(PageRequest request, Long roleId, SysUser searchUser, List<Long> deptIds);

    PageModel<SysUser> selectAllocatedList(PageRequest request, Long roleId, SysUser searchUser, List<Long> deptIds);

    long deleteUserByIds(String ids);

    void deleteAllUserInfoByUserId(Long userId);

    SysUser resetUserPwd(Long userId, String newPassword, String salt);

    String selectUserRoleGroup(Long userId);

    String selectUserPostGroup(Long userId);

    String importUser(List<SysUser> userList, boolean updateSupport, String operName);

    void checkUserAllowed(SysUser user);
}
