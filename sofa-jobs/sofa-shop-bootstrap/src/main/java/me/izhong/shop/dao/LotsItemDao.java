package me.izhong.shop.dao;

import me.izhong.shop.entity.LotsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LotsItemDao extends JpaRepository<LotsItem, Long>, JpaSpecificationExecutor {
}
