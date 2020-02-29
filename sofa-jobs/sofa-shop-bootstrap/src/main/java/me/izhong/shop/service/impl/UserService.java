package me.izhong.shop.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@Slf4j
public class UserService implements IUserService {

    @Autowired private UserDao userDao;
    @Autowired private ThirdPartyService certifyService;


    public void expectNew(User user) {
        if (!StringUtils.isEmpty(user.getLoginName()) &&
                userDao.findFirstByLoginName(user.getLoginName()) != null) {
            throw BusinessException.build("用户名已经存在:" + user.getLoginName());
        }
        if (!StringUtils.isEmpty(user.getEmail()) &&
                userDao.findFirstByEmail(user.getEmail())!=null) {
            throw BusinessException.build("邮箱已经被使用:" + user.getEmail());
        }
        if (!StringUtils.isEmpty(user.getPhone()) &&
                userDao.findFirstByPhone(user.getPhone())!=null) {
            throw BusinessException.build("该号码已被使用:" + user.getPhone());
        }
    }

    @Transactional
    @Override
    public User registerUser(User user) {
        if (userDao.findFirstByPhone(user.getPhone())!=null) {
            throw BusinessException.build("该号码已被使用:" + user.getPhone());
        }
        user.encryptUserPassword();
        user.setRegisterTime(Timestamp.valueOf(LocalDateTime.now()));
        user.setLoginTime(user.getRegisterTime());
        return userDao.save(user);
    }

    @Transactional
    @Override
    public User saveOrUpdate(User user) {
        return userDao.save(user);
    }

    public void certify(User user) {
        boolean certifiedRes = certifyService.getCertifiedInfo(user.getName(), user.getIdentityID());
        if (certifiedRes) {
            user.setIsCertified(true);
            userDao.save(user);
        }
    }

    @Override
    public User expectExists(User user) {
        User u = null;
        if (!StringUtils.isEmpty(user.getLoginName()) &&
                (u=userDao.findFirstByLoginName(user.getLoginName())) != null) {
            return u;
        } else if (!StringUtils.isEmpty(user.getEmail()) &&
                (u=userDao.findFirstByEmail(user.getEmail()))!=null) {
            return u;
        } else if (!StringUtils.isEmpty(user.getPhone()) &&
                (u=userDao.findFirstByPhone(user.getPhone()))!=null) {
            return u;
        }
        throw BusinessException.build("用户不存在");
    }

    @Override
    public void lock(Long userId, boolean isLock) {
        User user = findById(userId);
        user.setIsLocked(isLock);
        saveOrUpdate(user);
    }

    @Override
    public void attemptModifyPhone(User user, String newPhoneNumber) {
        if (StringUtils.isEmpty(newPhoneNumber)) {
            throw BusinessException.build("手机号不能为空");
        }
        if (!StringUtils.equals(newPhoneNumber, user.getPhone())) {
            User checkUser = userDao.findFirstByPhone(newPhoneNumber);
            if (checkUser != null && !checkUser.getId().equals(user.getId())) {
                throw BusinessException.build("手机号码 " + newPhoneNumber + " 已经被注册");
            }
            user.setPhone(newPhoneNumber);
        }
    }

    @Override
    public void attemptModifyEmail(User user, String email) {
        if (!StringUtils.isEmpty(email) && !StringUtils.equals(user.getPhone(), email)) {
            if (userDao.findFirstByEmail(email) != null ) {
                throw BusinessException.build(email  + " 已经被注册");
            }
        }
        user.setEmail(email);
    }

    @Override
    public void attemptModifyLoginName(User dbUser, String loginName) {
        if (!StringUtils.isEmpty(loginName) && !StringUtils.equals(dbUser.getLoginName(), loginName)) {
            if (userDao.findFirstByEmail(loginName) != null ) {
                throw BusinessException.build(loginName  + " 已经被注册");
            }
        }
        dbUser.setLoginName(loginName);
    }

    @Override
    public void attemptModifyPassword(User dbUser, String password) {
        if (!StringUtils.isEmpty(password) && !StringUtils.equals(dbUser.getPassword(), password)) {
            dbUser.setPassword(password);
            dbUser.encryptUserPassword();
        }
    }

    @Override
    public User findById(Long userId) {
        return userDao.findById(userId).orElseThrow(()->new RuntimeException("unable to find user by " + userId));
    }
}
