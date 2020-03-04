package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.GoodsCategory;

@Repository
public interface GoodsCategoryDao extends JpaRepository<GoodsCategory, Long> {

	@Query(value = "select t1.id, t1.name from product_category t1 left join product_category t2 on t2.parent_id = t1.id where t2.id = ?1", nativeQuery = true)
	GoodsCategory findByChildrenId(Long categoryId);

	List<GoodsCategory> findByParentId(Long parentId);

	List<GoodsCategory> findByLevelAndShowStatus(Integer level, Integer showStatus);

	@Modifying
	@Query(value = "update product_category t set t.show_status = ?2 where t.id in ?1", nativeQuery = true)
	void updateShowStatus(List<Long> ids, Integer showStatus);
}
