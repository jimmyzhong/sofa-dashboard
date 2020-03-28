package me.izhong.shop.dao;

import me.izhong.shop.entity.Job;
import me.izhong.shop.entity.UserMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;

@Repository
public interface JobDao extends JpaRepository<Job, Integer> {
    Job findFirstByName(String name);
    Job findFirstByNameAndLastRunTimeBefore(String name, LocalDateTime lastRunTime);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from Job t where t.id =?1 ")
    Job selectJobForUpdate(Integer jobId);
}
