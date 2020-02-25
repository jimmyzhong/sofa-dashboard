package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Goods;

@Repository
public interface GoodsDao extends JpaRepository<Goods, Long> {

	Goods findByName(String name);
}
