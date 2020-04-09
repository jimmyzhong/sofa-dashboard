package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.LotParamInfo;

@Repository
public interface LotParamInfoDao extends JpaRepository<LotParamInfo, Long> {
}
