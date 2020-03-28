package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Lots;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LotsDao extends JpaRepository<Lots, Long> {
    List<Lots> findAllByStartTimeBetweenOrderByStartTime(LocalDateTime from, LocalDateTime to);
}
