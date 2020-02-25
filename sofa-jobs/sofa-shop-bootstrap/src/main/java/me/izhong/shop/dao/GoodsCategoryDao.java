package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.GoodsCategory;

@Repository
public interface GoodsCategoryDao extends JpaRepository<GoodsCategory, Long> {
	
	@Modifying
	@Query(value = "update product_category t set t.show_status = ?1 where t.id in ?2", nativeQuery = true)
	void updateShowStatus(Integer showStatus, List<Long> ids);
}
