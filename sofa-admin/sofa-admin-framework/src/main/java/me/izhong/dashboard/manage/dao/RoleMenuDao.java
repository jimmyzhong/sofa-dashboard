package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysRoleMenu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMenuDao extends MongoRepository<SysRoleMenu, Long> {

    void deleteAllByRoleId(Long roleId);

    void deleteAllByMenuId(Long menuId);

    long deleteAllByRoleIdAndMenuId(Long roleId, Long menuId);

    int countByRoleId(Long roleId);

    int countByMenuId(Long menuId);

    List<SysRoleMenu> findAllByRoleId(Long roleId);

    List<SysRoleMenu> findAllByMenuId(Long menuId);
}
