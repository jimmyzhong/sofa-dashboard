package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysDept;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeptDao extends MongoRepository<SysDept, Long> {


    SysDept findByDeptId(Long deptId);

    List<SysDept> findAllByDeptIdIn(Long[] deptId);

    int countByParentId(Long parentId);

    List<SysDept> findAllByParentId(Long parentId);

    SysDept findByDeptNameAndParentId(String deptName, Long parentId);
}
