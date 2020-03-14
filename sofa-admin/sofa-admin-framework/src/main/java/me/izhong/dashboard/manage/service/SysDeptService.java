package me.izhong.dashboard.manage.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.dashboard.manage.entity.SysDept;
import me.izhong.dashboard.manage.domain.Ztree;


import java.util.List;

public interface SysDeptService extends CrudBaseService<Long,SysDept> {

    List<Long> selectAllDeptId();

    List<SysDept> selectDeptList(SysDept search);

    /**
     * 查询部门管理树
     *
     * @param sysDept 搜索条件，空查询所有的
     * @return 所有部门信息
     */
    public List<Ztree> selectDeptTree(SysDept sysDept);

    /**
     * 根据角色ID查询菜单
     *
     * @param roleId 角色Id
     * @return 菜单列表
     */
    public List<Ztree> roleDeptTreeData(Long roleId);

    /**
     * 查询部门人数
     *
     * @param parentId 父部门ID
     * @return 结果
     */
    public int selectDeptCount(Long parentId);

    public void checkExistChildDept(Long parentId);

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     */
    public void checkDeptExistUser(Long deptId);



    /**
     * 新增保存部门信息
     *
     * @param sysDept 部门信息
     * @return 结果
     */
    public int insertDept(SysDept sysDept);

    /**
     * 修改保存部门信息
     *
     * @param sysDept 部门信息
     * @return 结果
     */
    public int updateDept(SysDept sysDept);

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    public SysDept selectDeptByDeptId(Long deptId);

    /**
     * 校验部门名称是否唯一
     *
     * @param sysDept 部门信息
     * @return 结果
     */
    public boolean checkDeptNameUnique(SysDept sysDept);

    List<Ztree> selectDeptTreeData(Long[] deptIds);
}
