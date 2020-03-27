package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Lots;

@Repository
public interface LotsDao extends JpaRepository<Lots, Long> {
}
