package me.izhong.shop.dao;

import me.izhong.shop.entity.GoodsStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsStoreDao extends JpaRepository<GoodsStore, Long>{
    GoodsStore findByProductIdAndProductAttrId(Long productId, Long productAttrId);
}
