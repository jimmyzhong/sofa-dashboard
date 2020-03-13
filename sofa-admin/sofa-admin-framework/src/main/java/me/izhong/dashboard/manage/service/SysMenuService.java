package me.izhong.dashboard.manage.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.dashboard.manage.entity.SysMenu;
import me.izhong.dashboard.manage.domain.Ztree;

import java.util.List;
import java.util.Set;

public interface SysMenuService extends CrudBaseService<Long,SysMenu> {

    /**
     * 根据用户ID查询菜单 Visible可见的
     *
     * @return 菜单列表 返回的是数形结构
     */
    List<SysMenu> selectVisibleMenusByUser(Long userId);

    List<SysMenu> selectMenusByUser(Long userId);

    /**
     * 查询系统菜单列表
     *
     * @param sysMenu 菜单信息
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList(SysMenu sysMenu);

    /**
     * 查询菜单集合
     *
     * @return 所有菜单信息
     */
    public List<SysMenu> selectMenuAll();

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectPermsByUserId(Long userId);

    /**
     * 根据角色ID查询菜单
     *
     * @param roleId 角色Id
     * @return 菜单列表
     */
    public List<Ztree> roleMenuTreeData(Long roleId);

    /**
     * 根据角色获取角色下面的权限
     * @param roleId
     * @return
     */
    List<String> selectPermsByRoleId(Long roleId);

    /**
     * 查询所有菜单信息
     *
     * @return 菜单列表
     */
    public List<Ztree> menuTreeData();

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    public SysMenu selectMenuById(Long menuId);

    /**
     * 查询菜单数量
     *
     * @param parentId 菜单父ID
     * @return 结果
     */
    public int selectCountMenuByParentId(Long parentId);

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public int selectCountRoleMenuByMenuId(Long menuId);

    /**
     * 新增保存菜单信息
     *
     * @param sysMenu 菜单信息
     * @return 结果
     */
    public int insertMenu(SysMenu sysMenu);

    /**
     * 修改保存菜单信息
     *
     * @param sysMenu 菜单信息
     * @return 结果
     */
    public int updateMenu(SysMenu sysMenu);

    /**
     * 校验菜单名称是否唯一
     *
     * @param sysMenu 菜单信息
     * @return 结果
     */
    public boolean checkMenuNameUnique(SysMenu sysMenu);
}
