package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.PlatformParamConfig;

@Repository
public interface PlatformParamConfigDao extends JpaRepository<PlatformParamConfig, Long> {
}
