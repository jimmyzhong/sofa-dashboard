package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.UserCollection;

@Repository
public interface CollectionDao extends JpaRepository<UserCollection, Long> {

	UserCollection findByUserIdAndProductId(Long userId, Long productId);

	int deleteByUserIdAndProductId(Long userId, Long productId);

	List<UserCollection> findByUserId(Long userId);
}
