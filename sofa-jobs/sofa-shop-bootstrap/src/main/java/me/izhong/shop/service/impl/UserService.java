package me.izhong.shop.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService implements IUserService {
    private UserDao userDao;
    private ThirdPartyService certifyService;

    @Autowired
    public UserService(UserDao userDao, ThirdPartyService certifyService) {
        this.userDao = userDao;
        this.certifyService = certifyService;
    }

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
        if (userDao.findFirstByUserName(user.getUserName()) != null) {
            throw new RuntimeException("用户名已经存在:" + user.getUserName());
        } else if (userDao.findFirstByEmail(user.getEmail())!=null) {
            throw new RuntimeException("用户名已经存在:" + user.getEmail());
        } else if (userDao.findFirstByEmail(user.getPhone())!=null) {
            throw new RuntimeException("该号码已被使用:" + user.getPhone());
        }
    }

    @Override
    public User saveOrUpdate(User user) {
        return userDao.save(user);
    }

    public void certify(User user) {
        String certifiedRes = certifyService.getCertifiedInfo(user.getName(), user.getIdentityID());
        if (!StringUtils.isEmpty(certifiedRes)) {
            user.setIsCertified(true);
            userDao.save(user);
        }
    }

    @Override
    public User findById(Long userId) {
        return userDao.findById(userId).get();
    }
}
