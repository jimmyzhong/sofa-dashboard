package me.izhong.shop.service;

import me.izhong.shop.entity.User;

public interface IUserService {

    User saveOrUpdate(User user);

    User findById(Long userId);

    void expectNew(User user);

    void certify(User user);

    User expectExists(User user);
}
