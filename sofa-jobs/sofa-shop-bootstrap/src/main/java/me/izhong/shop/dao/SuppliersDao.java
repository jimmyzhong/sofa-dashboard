package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Suppliers;

@Repository
public interface SuppliersDao extends JpaRepository<Suppliers, Long>, JpaSpecificationExecutor<Suppliers> {
}
