package me.izhong.shop.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.exception.BusinessException;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserService implements IUserService {

    @Autowired private UserDao userDao;
    @Autowired private ThirdPartyService certifyService;

    public void validateRole(String[] requiredRoles, String userId) {
        Optional<User> user = userDao.findById(Long.valueOf(userId));
        if (!user.isPresent()) {
            log.warn("unable to find user by id " + userId);
            throw new RuntimeException("unable to find user.");
        }

//        if (requiredRoles!=null && requiredRoles.length > 0) {
//            Set<Role> roles = user.get().getRoles();
//            if (roles == null || roles.isEmpty()) {
//                throw new RuntimeException("access denied. require " + Arrays.asList(requiredRoles).stream()
//                        .collect(Collectors.joining(",")));
//            }
//
//            for (String roleName: requiredRoles) {
//                if(!StringUtils.isEmpty(roleName) && !roles.stream()
//                        .anyMatch(r->roleName.equalsIgnoreCase(r.getName()))){
//                    throw new RuntimeException("insufficient privilege. require " + roleName);
//                }
//            }
//        }
    }

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
    public User findById(Long userId) {
        return userDao.findById(userId).get();
    }
}
