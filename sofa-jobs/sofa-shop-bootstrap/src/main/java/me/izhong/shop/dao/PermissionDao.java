package me.izhong.shop.dao;

import me.izhong.shop.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionDao extends JpaRepository<Permission, Integer> {
}
