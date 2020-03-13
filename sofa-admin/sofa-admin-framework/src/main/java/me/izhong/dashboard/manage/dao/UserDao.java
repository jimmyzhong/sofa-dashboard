package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao extends MongoRepository<SysUser, Long> {

    SysUser findByUserId(Long userId);

    List<SysUser> findAllByDeptId(Long deptId);

    List<SysUser> findAllByUserIdIn(Long[] userIds);

    SysUser findByLoginName(String loginName);

    List<SysUser> findByPhoneNumber(String phoneNumber);

    SysUser findByOpenId(String openId);

    List<SysUser> findByEmail(String email);
}
