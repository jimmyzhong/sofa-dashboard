package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysMenu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuDao extends MongoRepository<SysMenu, Long> {


    SysMenu findByMenuId(Long menuId);

    SysMenu findByMenuName(String menuName);

    List<SysMenu> findAllByParentId(Long parentId);

    List<SysMenu> findAllByParentIdAndIsDelete(Long parentId,boolean isDelete);

    List<SysMenu> findAllByMenuIdIn(List<Long> menuIds);
}
