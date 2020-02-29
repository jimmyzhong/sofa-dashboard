package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.City;

@Repository
public interface CityDao extends JpaRepository<City, Long> {

	List<City> findByProvinceCode(String provinceCode);
}
