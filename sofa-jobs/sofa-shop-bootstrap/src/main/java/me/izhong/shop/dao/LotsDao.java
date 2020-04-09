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
import java.util.Map;

@Repository
public interface LotsDao extends JpaRepository<Lots, Long>, JpaSpecificationExecutor {


    List<Lots> findAllByStartTimeBetweenAndUploadedOrderByStartTime(LocalDateTime from, LocalDateTime to, Integer uploaded);
    List<Lots> findAllByEndTimeBeforeAndFollowCountGreaterThanAndPayStatusIsNull(
            LocalDateTime endTime, Integer followCount);

    @Query(value = "select au.* from lots au, tx_order o, user u where o.user_id = u.id and o.order_type = ?2 " +
            "and u.id = ?1 and o.status = ?3 and au.id=o.auction_id ORDER BY ?#{#pageable}", nativeQuery = true)
    Page<Lots> findAllByUser(Long userId, Integer orderType, Integer orderStatus, Pageable pageable);

    // TODO jpa query is better
    @Query(value = "select au.*, o.order_type, o.status order_status from lots au, tx_order o, user u where o.user_id = u.id and au.id=o.auction_id and u.id = ?1 and  " +
            "( ( o.order_type = 4 and o.status = 1 and START_TIME > ?7 and 1 = ?2 ) or " +
            " ( o.order_type = 4 and o.status = 1 and START_TIME < ?7 and END_TIME > ?7 and 1 = ?3 ) " +
            "or ( o.order_type = 5 AND o.status = 10 and 1 = ?4 ) " +
            "or ( o.order_type = 5 AND o.status = 1 and 1 = ?5 ) " +
            "or ( (au.pay_status=3 or au.pay_status=4 or au.pay_status=1) and 1 = ?6 ) ) ",
            //"ORDER BY ?#{#pageable}",
            countQuery = "select count(au.id) from lots au, tx_order o, user u where o.user_id = u.id and au.id=o.auction_id and u.id = ?1 and  " +
                    "( ( o.order_type = 4 and o.status = 1 and START_TIME > ?7 and 1 = ?2 ) or " +
                    " ( o.order_type = 4 and o.status = 1 and START_TIME < ?7 and END_TIME > ?7 and 1 = ?3 ) " +
                    "or ( o.order_type = 5 AND o.status = 10 and 1 = ?4 ) " +
                    "or ( o.order_type = 5 AND o.status = 1 and 1 = ?5 ) " +
                    "or ( (au.pay_status=3 or au.pay_status=4 or au.pay_status=1) and 1 = ?6 ) ) ",
            nativeQuery = true)
    Page<Map<String, Object>> listOfUser(Long userId,
                                         Integer signUpNotStart, Integer signUpOnGoing,
                                         Integer fetchDealNotPaid, Integer fetchDealPaid, Integer done,
                                         LocalDateTime currentTime, Pageable pageable);


    @Query(value = "select t.password from Lots t where t.lotsNo = ?1")
    String getPasswordOfLots(String lotsNo);

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
