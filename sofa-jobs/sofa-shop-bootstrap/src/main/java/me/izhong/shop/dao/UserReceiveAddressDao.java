package me.izhong.shop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.UserReceiveAddress;

@Repository
public interface UserReceiveAddressDao extends JpaRepository<UserReceiveAddress, Long> {

    void deleteByUserIdAndId(Long userId, Long id);

    List<UserReceiveAddress> findByUserId(Long userId);

    UserReceiveAddress findByUserIdAndId(Long userId, Long id);

    UserReceiveAddress findByUserIdAndIsDefault(Long userId, Integer isDefault);
}
