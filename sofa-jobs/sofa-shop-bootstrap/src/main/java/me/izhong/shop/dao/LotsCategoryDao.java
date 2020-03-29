package me.izhong.shop.dao;

import io.swagger.models.auth.In;
import me.izhong.shop.entity.LotsCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotsCategoryDao extends JpaRepository<LotsCategory, Integer> {
}
