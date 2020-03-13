package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysUserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleDao extends MongoRepository<SysUserRole, Long> {

    long deleteAllByUserId(Long userId);

    long deleteAllByUserIdAndRoleId(Long userId, Long roleId);

    int countByRoleId(Long roleId);

    List<SysUserRole> findAllByRoleIdAndUserIdIn(Long roleId, Long[] longs);

    List<SysUserRole> findAllByRoleIdInAndUserId(List<Long> roleIds, Long userId);

}
