package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Province;

@Repository
public interface ProvinceDao extends JpaRepository<Province, Long> {
}
