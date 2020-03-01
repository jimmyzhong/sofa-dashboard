package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Goods;

@Repository
public interface GoodsDao extends JpaRepository<Goods, Long> {

	Goods findByProductName(String name);

	@Modifying
	@Query(value = "update product t set t.publish_status = ?2 where t.id in ?1", nativeQuery = true)
	void updatePublishStatus(List<Long> ids, Integer publishStatus);

	@Modifying
	@Query(value = "update product t set t.recommand_status = ?2 where t.id in ?1", nativeQuery = true)
	void updateRecommendStatus(List<Long> ids, Integer recommendStatus);

	@Modifying
	@Query(value = "update product t set t.is_delete = ?2 where t.id in ?1", nativeQuery = true)
	void updateIsDelete(List<Long> ids, Integer deleteStatus);
}
