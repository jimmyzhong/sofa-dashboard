package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.County;

@Repository
public interface CountyDao extends JpaRepository<County, Long> {

	List<County> findByCityCode(String cityCode);
}
