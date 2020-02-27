package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.GoodsCategory;

@Repository
public interface GoodsCategoryDao extends JpaRepository<GoodsCategory, Long> {

	List<GoodsCategory> findByParentId(Long parentId);

	List<GoodsCategory> findByLevelAndShowStatus(Integer level, Integer showStatus);

	@Modifying
	@Query(value = "update product_category t set t.show_status = ?2 where t.id in ?1", nativeQuery = true)
	void updateShowStatus(List<Long> ids, Integer showStatus);
}
