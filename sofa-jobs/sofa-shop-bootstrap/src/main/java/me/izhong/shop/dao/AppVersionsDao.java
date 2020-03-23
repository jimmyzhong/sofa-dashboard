package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.AppVersions;

@Repository
public interface AppVersionsDao extends JpaRepository<AppVersions, Long> {
}
