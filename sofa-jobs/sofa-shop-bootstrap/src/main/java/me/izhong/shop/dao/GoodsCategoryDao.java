package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.GoodsCategory;

@Repository
public interface GoodsCategoryDao extends JpaRepository<GoodsCategory, Long> {
}
