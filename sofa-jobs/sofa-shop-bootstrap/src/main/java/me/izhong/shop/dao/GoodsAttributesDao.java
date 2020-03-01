package me.izhong.shop.dao;

import me.izhong.shop.entity.GoodsAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsAttributesDao extends JpaRepository<GoodsAttributes, Long> {

    List<GoodsAttributes> findGoodsAttributesByProductId(Long productId);
}
