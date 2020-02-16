package me.izhong.shop.dao;

import me.izhong.shop.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserDao extends CrudRepository<User, Long> {
}
