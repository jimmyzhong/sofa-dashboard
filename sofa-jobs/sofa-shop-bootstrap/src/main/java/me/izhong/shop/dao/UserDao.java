package me.izhong.shop.dao;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.User;

@Repository
public interface UserDao extends JpaRepository<User, Long>, JpaSpecificationExecutor {

    User findFirstByEmail(String email);
    User findFirstByPhone(String phone);
    User findFirstByLoginName(String loginName);

    @Query(value = "select count(*) from user t where t.register_time = ?1", nativeQuery = true)
    long countUserByRegisterTime(LocalDateTime registerTime);
}
