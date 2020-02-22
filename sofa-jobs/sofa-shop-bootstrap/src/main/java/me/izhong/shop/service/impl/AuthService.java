package me.izhong.shop.service.impl;

import me.izhong.db.common.exception.BusinessException;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import me.izhong.shop.util.PasswordUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

@Service
public class AuthService {
    private UserDao userDao;
    @Autowired
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User attemptLogin(String username, String password) {
        User persistedUser = userDao.findFirstByLoginName(username);
        if (persistedUser == null) {
            persistedUser = userDao.findFirstByEmail(username);
        }
        if (persistedUser == null) {
            persistedUser = userDao.findFirstByPhone(username);
        }

        if (persistedUser == null) {
            throw BusinessException.build("用户不存在");
        }

        String encryptedPass = PasswordUtils.encrypt(password, persistedUser.getSalt());
        if (!persistedUser.getPassword().equalsIgnoreCase(encryptedPass)) {
            throw BusinessException.build("用户名密码不匹配");
        }
        return persistedUser;
    }


}
