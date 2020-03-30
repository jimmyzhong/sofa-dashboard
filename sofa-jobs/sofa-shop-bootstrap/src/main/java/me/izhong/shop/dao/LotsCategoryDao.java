package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.LotsCategory;

@Repository
public interface LotsCategoryDao extends JpaRepository<LotsCategory, Integer>, JpaSpecificationExecutor<LotsCategory> {
}
