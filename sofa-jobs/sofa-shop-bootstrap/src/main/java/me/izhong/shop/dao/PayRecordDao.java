package me.izhong.shop.dao;

import me.izhong.shop.entity.PayRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayRecordDao extends JpaRepository<PayRecord, Long> {

    PayRecord findFirstByInternalId(String internalOrderNo);
}
