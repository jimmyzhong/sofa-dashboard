package me.izhong.shop.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Lots;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LotsDao extends JpaRepository<Lots, Long> {
    List<Lots> findAllByStartTimeBetweenOrderByStartTime(LocalDateTime from, LocalDateTime to);

    @Query(value = "select au.* from lots au, tx_order o, user u where o.user_id = u.id and o.order_type = ?2 " +
            "and u.id = ?1 and o.status = ?3 and au.id=o.auction_id ORDER BY ?#{#pageable}", nativeQuery = true)
    Page<Lots> findAllByUser(Long userId, Integer orderType, Integer orderStatus, Pageable pageable);
}
