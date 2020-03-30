package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Ad;

@Repository
public interface AdDao extends JpaRepository<Ad, Long>, JpaSpecificationExecutor<Ad> {

    @Modifying
    @Query(value = "update ad t set t.status = ?2 where t.id = ?1", nativeQuery = true)
	void updateStatusById(Long id, Integer status);
}
