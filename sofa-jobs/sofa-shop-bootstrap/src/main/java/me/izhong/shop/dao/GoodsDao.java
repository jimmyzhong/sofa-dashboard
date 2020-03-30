package me.izhong.shop.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Goods;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GoodsDao extends JpaRepository<Goods, Long>, JpaSpecificationExecutor<Goods> {

	Goods findByProductName(String name);

	List<Goods> findAllByProductTypeAndCreateTimeBeforeAndCreatedByIsNotNullAndStockGreaterThan(
			Integer productType, LocalDateTime time, Integer stock);

	@Modifying
	@Query(value = "update product t set t.price = ?2 where t.id = ?1", nativeQuery = true)
	@Transactional
	void updateProductPrice(Long productId, BigDecimal price);

	@Modifying
	@Query(value = "update product t set t.stock = ?2, t.sale = ?3 where t.id = ?1", nativeQuery = true)
	@Transactional
	void updateProductStockAndSale(Long productId, Integer stock, Integer sale);

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
