package me.izhong.dashboard.manage.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.dashboard.manage.entity.SysUserRole;
import me.izhong.common.exception.BusinessException;

import java.util.List;
import java.util.Set;

public interface SysRoleService extends CrudBaseService<Long,SysRole> {

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectRoleKeys(Long userId);


    List<Long> selectDeptIdsByRoleId(Long roleId);

    /**
     * 根据用户ID查询角色，所有的 有check
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<SysRole> selectAllRolesByUserId(Long userId);

    public List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public long deleteRoleById(Long roleId);

    /**
     * 批量删除角色用户信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     * @throws Exception 异常
     */
    public long removeRoleInfo(String ids) throws BusinessException;

    /**
     * 新增保存角色信息
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    public int insertRole(SysRole sysRole);

    /**
     * 修改保存角色信息
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    public int updateRole(SysRole sysRole);

    /**
     * 修改数据权限信息
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    public int authDataScope(SysRole sysRole);

    /**
     * 校验角色名称是否唯一
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    public boolean checkRoleNameUnique(SysRole sysRole);

    /**
     * 校验角色权限是否唯一
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    public boolean checkRoleKeyUnique(SysRole sysRole);

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public int countUserRoleByRoleId(Long roleId);

    /**
     * 角色状态修改
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    public int changeStatus(SysRole sysRole);

    /**
     * 取消授权用户角色
     *
     * @param sysUserRole 用户和角色关联信息
     * @return 结果
     */
    public long deleteAuthUser(SysUserRole sysUserRole);

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    public long deleteAuthUsers(Long roleId, String userIds);

    public long deleteAuthUsers(List<Long> roleId, Long userId);
    public long deleteAuthUsers(Long userId);

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    public int insertAuthUsers(Long roleId, String userIds);

    void checkRoleAllowed(SysRole sysRole);
}
