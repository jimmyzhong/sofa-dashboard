package me.izhong.shop.dao;

import me.izhong.shop.entity.UserMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

@Repository
public interface UserMoneyDao extends JpaRepository<UserMoney, Long> {
    UserMoney findByUserId(Long userId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from UserMoney t where t.userId =?1 ")
    UserMoney selectUserForUpdate(Long userId);
}
