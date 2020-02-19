package me.izhong.shop.dao;

import me.izhong.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {

    User findFirstByUserName(String userName);
    User findFirstByEmail(String email);
    User findFirstByPhone(String phone);

}
