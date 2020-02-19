package me.izhong.shop.service.impl;

import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private UserDao userDao;
    @Autowired
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User attemptLogin(User user) {
        User persistedUser = userDao.findFirstByUserName(user.getUserName());
        if (persistedUser == null) {
            persistedUser = userDao.findFirstByEmail(user.getUserName());
        }
        if (persistedUser == null) {
            persistedUser = userDao.findFirstByPhone(user.getUserName());
        }

        if (persistedUser == null) {
            throw new RuntimeException("Unable to find User.");
        }

        return persistedUser;
    }


}
