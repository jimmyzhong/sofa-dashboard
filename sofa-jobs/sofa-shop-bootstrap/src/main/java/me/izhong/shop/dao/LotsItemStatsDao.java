package me.izhong.shop.dao;

import me.izhong.shop.entity.LotsItemStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LotsItemStatsDao extends JpaRepository<LotsItemStats, Long> {
    @Query(value = "select s.* from lots au, LOTS_ITEM_STATS s where s.lots_id = au.id and au.lots_no = ?1" +
            " ORDER BY ?#{#pageable}", nativeQuery = true)
    Page<LotsItemStats> findByLotsNo(String lotsNo, Pageable pageable);
}
