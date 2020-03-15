package me.izhong.shop.dao;

import me.izhong.shop.entity.PayRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PayRecordDao extends JpaRepository<PayRecord, Long>, JpaSpecificationExecutor {

    PayRecord findFirstByInternalIdAndType(String internalOrderNo, String type);
}
