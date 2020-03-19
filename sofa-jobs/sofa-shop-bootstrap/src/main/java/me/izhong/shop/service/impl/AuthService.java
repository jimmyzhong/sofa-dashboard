package me.izhong.shop.service.impl;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import me.izhong.shop.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class AuthService {
    private UserDao userDao;
    @Autowired
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User attemptLogin(String username, String password) {
//        User persistedUser = userDao.findFirstByLoginName(username);
//        if (persistedUser == null) {
//            persistedUser = userDao.findFirstByEmail(username);
//        }
//        if (persistedUser == null) {
//            persistedUser = userDao.findFirstByPhone(username);
//        }

        User persistedUser = userDao.findFirstByPhone(username);

        if (persistedUser == null) {
            throw BusinessException.build("登录手机号不存在，请先注册");
        }

        String encryptedPass = PasswordUtils.encrypt(password, persistedUser.getSalt());
        if (!persistedUser.getPassword().equalsIgnoreCase(encryptedPass)) {
            throw BusinessException.build("用户名密码不匹配");
        }

        LocalDateTime now = LocalDateTime.now();
        persistedUser.setLoginTime(Timestamp.valueOf(now));

        userDao.save(persistedUser);
        return persistedUser;
    }


}
