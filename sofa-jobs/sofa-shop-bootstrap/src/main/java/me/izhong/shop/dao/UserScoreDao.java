package me.izhong.shop.dao;

import me.izhong.shop.entity.UserMoney;
import me.izhong.shop.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

@Repository
public interface UserScoreDao extends JpaRepository<UserScore, Long> {
    UserScore findByUserId(Long userId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from UserScore t where t.userId =?1 ")
    UserScore selectUserForUpdate(Long userId);
}
