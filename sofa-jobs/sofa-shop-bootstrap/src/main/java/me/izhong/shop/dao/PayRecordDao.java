package me.izhong.shop.dao;

import me.izhong.shop.entity.PayRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface PayRecordDao extends JpaRepository<PayRecord, Long>, JpaSpecificationExecutor {

    PayRecord findFirstByInternalIdAndType(String internalOrderNo, String type);


    @Query(value = "select distinct(pr.receiver) from pay_record pr where pr.create_time > ?1 and pr.create_time <= ?2 and pr.sys_state = ?3", nativeQuery = true)
    Set<Long> findReceiversBetweenCreationDateWithSysState(LocalDateTime start, LocalDateTime end, Integer sysState);

    @Query(value = "select pr.* from pay_record pr where pr.receiver = ?1 and pr.create_time > ?2 and pr.create_time <= ?3 and pr.sys_state = ?4", nativeQuery = true)
    List<PayRecord> findAllByReceiverAndBetweenCreationDate(Long receiver, LocalDateTime start, LocalDateTime end, Integer sysState);
}
