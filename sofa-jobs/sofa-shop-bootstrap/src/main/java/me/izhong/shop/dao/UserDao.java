package me.izhong.shop.dao;

import me.izhong.shop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao extends JpaRepository<User, Long>, JpaSpecificationExecutor {

    User findFirstByEmail(String email);
    User findFirstByPhone(String phone);
    User findFirstByLoginName(String loginName);
}
