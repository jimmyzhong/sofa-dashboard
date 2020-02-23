package me.izhong.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.entity.SysMenu;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.service.SysMenuService;
import me.izhong.dashboard.manage.domain.Ztree;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/system/menu")
public class MenuAdminController {

    private String prefix = "system/menu";

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping()
    public String menu() {
        return prefix + "/menu";
    }

    @RequiresPermissions(PermissionConstants.Menu.VIEW)
    @PostMapping("/list")
    @ResponseBody
    public List<SysMenu> list(SysMenu sysMenu) {
        sysMenu.setIsDelete(false);
        List<SysMenu> sysMenuList = sysMenuService.selectMenuList(sysMenu);
        return sysMenuList;
    }

    /**
     * 删除菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @RequiresPermissions(PermissionConstants.Menu.REMOVE)
    @GetMapping("/remove/{menuId}")
    @AjaxWrapper
    public long remove(@PathVariable("menuId") Long menuId) {
        if (sysMenuService.selectCountMenuByParentId(menuId) > 0) {
            throw BusinessException.build("存在子菜单,不允许删除");
        }
        if (sysMenuService.selectCountRoleMenuByMenuId(menuId) > 0) {
            throw BusinessException.build("菜单已分配,不允许删除");
        }
        return sysMenuService.deleteByPId(menuId);
    }

    /**
     * 新增
     */
    @GetMapping("/add/{parentId}")
    public String add(@PathVariable("parentId") Long parentId, ModelMap mmap) {
        SysMenu sysMenu = null;
        if (0L != parentId) {
            sysMenu = sysMenuService.selectMenuById(parentId);
        } else {
            sysMenu = new SysMenu();
            sysMenu.setMenuId(0L);
            sysMenu.setMenuName("根目录");
        }
        mmap.put("menu", sysMenu);
        return prefix + "/add";
    }

    /**
     * 新增保存菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.ADD)
    @RequiresPermissions(PermissionConstants.Menu.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public int addSave(@Validated SysMenu sysMenu) {
        if (!sysMenuService.checkMenuNameUnique(sysMenu)) {
            throw BusinessException.build("新增菜单'" + sysMenu.getMenuName() + "'失败，菜单名称已存在");
        }
        return sysMenuService.insertMenu(sysMenu);
    }

    /**
     * 修改菜单
     */
    @GetMapping("/edit/{menuId}")
    public String edit(@PathVariable("menuId") Long menuId, ModelMap mmap) {
        SysMenu sysMenu = sysMenuService.selectMenuById(menuId);
        mmap.put("menu", sysMenu);
        if (0L == sysMenu.getParentId()) {
            sysMenu.setParentId(0L);
            sysMenu.setParentName("根目录");
        }
        mmap.put("menu", sysMenu);
        return prefix + "/edit";
    }

    /**
     * 修改保存菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions(PermissionConstants.Menu.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public int editSave(@Validated SysMenu sysMenu) {
        if (!sysMenuService.checkMenuNameUnique(sysMenu)) {
            throw BusinessException.build("修改菜单'" + sysMenu.getMenuName() + "'失败，菜单名称已存在");
        }
        return sysMenuService.updateMenu(sysMenu);
    }

    /**
     * 选择菜单图标
     */
    @GetMapping("/icon")
    public String icon() {
        return prefix + "/icon";
    }

    /**
     * 校验菜单名称
     */
    @PostMapping("/checkMenuNameUnique")
    @ResponseBody
    public boolean checkMenuNameUnique(SysMenu sysMenu) {
        return sysMenuService.checkMenuNameUnique(sysMenu);
    }

    /**
     * 加载角色菜单列表树, 新建，编辑role页面
     */
    @GetMapping("/roleMenuTreeData")
    @ResponseBody
    public List<Ztree> roleMenuTreeData(Long roleId) {
        List<Ztree> ztrees = sysMenuService.roleMenuTreeData(roleId);
        return ztrees;
    }

    /**
     * 加载所有菜单列表树
     */
    @GetMapping("/menuTreeData")
    @ResponseBody
    public List<Ztree> menuTreeData(SysRole sysRole) {
        List<Ztree> ztrees = sysMenuService.menuTreeData();
        return ztrees;
    }

    /**
     * 选择菜单树
     */
    @GetMapping("/selectMenuTree/{menuId}")
    public String selectMenuTree(@PathVariable("menuId") Long menuId, ModelMap mmap) {
        SysMenu menu;
        if(menuId.equals(0L)) {
            menu = new SysMenu();
            menu.setMenuId(0L);
            menu.setMenuName("主目录");
        } else {
            menu = sysMenuService.selectMenuById(menuId);
        }
        mmap.put("menu", menu);
        return prefix + "/tree";
    }

}
