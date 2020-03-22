package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Versions;

@Repository
public interface VersionsDao extends JpaRepository<Versions, Long> {
}
