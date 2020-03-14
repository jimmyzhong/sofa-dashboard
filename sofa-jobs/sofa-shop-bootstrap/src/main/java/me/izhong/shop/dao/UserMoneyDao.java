package me.izhong.shop.dao;

import me.izhong.shop.entity.UserMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMoneyDao extends JpaRepository<UserMoney, Long> {
    UserMoney findByUserId(Long userId);
}
