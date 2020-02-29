package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Town;

@Repository
public interface TownDao extends JpaRepository<Town, Long> {

	List<Town> findByCountyCode(String countyCode);
}
