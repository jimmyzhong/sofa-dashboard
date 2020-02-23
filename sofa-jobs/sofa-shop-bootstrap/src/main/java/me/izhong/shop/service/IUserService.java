package me.izhong.shop.service;

import me.izhong.shop.entity.User;

public interface IUserService {

    User saveOrUpdate(User user);

    User findById(Long userId);

    void expectNew(User user);

    User registerUser(User user);

    void certify(User user);

    User expectExists(User user);

    void lock(Long userId, boolean isLock);

    void attemptModifyPhone(User user, String newPhoneNumber);

    void attemptModifyEmail(User user, String email);

    void attemptModifyLoginName(User dbUser, String loginName);

    void attemptModifyPassword(User dbUser, String password);
}
