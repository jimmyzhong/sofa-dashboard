package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Ad;

@Repository
public interface AdDao extends JpaRepository<Ad, Long> {

    @Modifying
    @Query(value = "update ad t set t.status = ?2 where t.id in ?1", nativeQuery = true)
	void updateStatusByIds(List<Long> ids, Integer status);
}
