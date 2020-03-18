package me.izhong.shop.dao;

import me.izhong.shop.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserScoreDao extends JpaRepository<UserScore, Long> {
    UserScore findByUserId(Long userId);
}
