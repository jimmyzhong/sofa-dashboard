package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.ConsignmentRule;

@Repository
public interface ConsignmentRuleDao extends JpaRepository<ConsignmentRule, Long> {
    ConsignmentRule findFirstByIsDeleteIsNullOrIsDeleteOrderByCreateTime(Integer isDelete);
}
