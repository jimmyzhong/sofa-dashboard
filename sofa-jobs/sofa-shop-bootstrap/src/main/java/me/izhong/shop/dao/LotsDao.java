package me.izhong.shop.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Lots;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LotsDao extends JpaRepository<Lots, Long>, JpaSpecificationExecutor {
    List<Lots> findAllByStartTimeBetweenAndUploadedOrderByStartTime(LocalDateTime from, LocalDateTime to, Integer uploaded);
    List<Lots> findAllByStartTimeLessThanEqualAndUploaded(LocalDateTime startTime, Integer uploaded);

    @Query(value = "select au.* from lots au, tx_order o, user u where o.user_id = u.id and o.order_type = ?2 " +
            "and u.id = ?1 and o.status = ?3 and au.id=o.auction_id ORDER BY ?#{#pageable}", nativeQuery = true)
    Page<Lots> findAllByUser(Long userId, Integer orderType, Integer orderStatus, Pageable pageable);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from Lots t where t.id =?1 ")
    Lots selectForUpdate(Long id);

    @Modifying
    @Transactional
    @Query(value = "update Lots t set t.followCount = ?2 where t.id = ?1 and t.followCount= ?3")
    int updateFollowCount(Long id, Integer newCount, Integer oldCount);

    @Modifying
    @Transactional
    @Query(value = "update Lots t set t.uploaded = 1,t.uploadedTime=?2,t.uploadedMsg=?3 where t.id = ?1")
    void markAsUploadedSuccess(Long id, LocalDateTime time, String msg);

    @Modifying
    @Transactional
    @Query(value = "update Lots t set t.uploaded = 2,t.uploadedTime=?2,t.uploadedMsg=?3 where t.id = ?1")
    void markAsUploadedFail(Long id, LocalDateTime time, String msg);

    Lots findFirstByLotsNo(String lotsNo);
}
