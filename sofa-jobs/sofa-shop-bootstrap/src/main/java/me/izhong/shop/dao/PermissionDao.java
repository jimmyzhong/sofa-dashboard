package me.izhong.shop.dao;

import me.izhong.shop.entity.Permission;
import org.springframework.data.repository.CrudRepository;

public interface PermissionDao extends CrudRepository<Permission, Integer> {
}
