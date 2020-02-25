package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Goods;

@Repository
public interface GoodsDao extends JpaRepository<Goods, Long> {

	Goods findByName(String name);

	@Modifying
	@Query(value = "update product t set t.publish_status = ?1 where t.id in ?2", nativeQuery = true)
	void updatePublishStatus(Integer publishStatus, List<Long> ids);

	@Modifying
	@Query(value = "update product t set t.recommand_status = ?1 where t.id in ?2", nativeQuery = true)
	void updateRecommendStatus(Integer recommendStatus, List<Long> ids);

	@Modifying
	@Query(value = "update product t set t.is_delete = ?1 where t.id in ?2", nativeQuery = true)
	void updateIsDelete(Integer deleteStatus, List<Long> ids);
}
