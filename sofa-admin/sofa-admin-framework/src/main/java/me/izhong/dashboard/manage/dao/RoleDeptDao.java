package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysRoleDept;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleDeptDao extends MongoRepository<SysRoleDept, Long> {

    void deleteAllByRoleId(Long roleId);

    long deleteAllByRoleIdAndDeptId(Long roleId, Long deptId);

    int countByRoleId(Long roleId);

    List<SysRoleDept> findAllByRoleId(Long roleId);
}
