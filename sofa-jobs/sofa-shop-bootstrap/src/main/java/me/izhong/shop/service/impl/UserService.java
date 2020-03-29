package me.izhong.shop.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.consts.ErrorCode;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.dao.UserMoneyDao;
import me.izhong.shop.dao.UserScoreDao;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.*;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.util.ShareCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements IUserService {

    @Autowired private UserDao userDao;
    @Autowired private UserMoneyDao userMoneyDao;
    @Autowired private UserScoreDao userScoreDao;
    @Autowired private PayRecordService payRecordService;
    @Autowired private ThirdPartyService certifyService;
    @Value("${user.avatar.default}")
    private String defaultAvatar;

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
        if (user.getInviteUserId() != null) {
            User invitor = userDao.getOne(user.getInviteUserId());
            if (invitor == null) {
                throw BusinessException.build("邀请人不存在");
            }
            if (invitor.getInviteUserId() != null) {
                user.setInviteUserId2(invitor.getInviteUserId());
            }
        }

        user.setAvatar(defaultAvatar);
        user.encryptUserPassword();
        user.setRegisterTime(Timestamp.valueOf(LocalDateTime.now()));
        user.setLoginTime(user.getRegisterTime());
        user =  userDao.save(user);
        user.setUserCode(ShareCodeUtil.generateUserCode(user.getId()));
        // TODO create money score table
        UserMoney money = new UserMoney();
        money.setUserId(user.getId());
        money.setAvailableAmount(BigDecimal.ZERO);
        money.setUnavailableAmount(BigDecimal.ZERO);
        money.setCreateTime(LocalDateTime.now());
        userMoneyDao.save(money);

        UserScore score = new UserScore();
        score.setUserId(user.getId());
        score.setAvailableScore(0L);
        score.setUnavailableScore(0L);
        userScoreDao.save(score);

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public PageModel<User> list(Long userId, PageQueryParamDTO request) {
        User user = this.findById(userId);

        User u = new User();
        if (request.getInviteUserId() != null) {
            u.setInviteUserId(request.getInviteUserId());
        }
        if (request.getInviteUserId2() != null) {
            u.setInviteUserId2(request.getInviteUserId2());
        }

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withMatcher("inviteUserId", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("inviteUserId2", ExampleMatcher.GenericPropertyMatchers.exact());

        if (request.getInviteUserId() == null && request.getInviteUserId2() == null) {
            u.setInviteUserId(userId);
            u.setInviteUserId2(userId);
            matcher = ExampleMatcher.matchingAny()
                    .withMatcher("inviteUserId", ExampleMatcher.GenericPropertyMatchers.exact())
                    .withMatcher("inviteUserId2", ExampleMatcher.GenericPropertyMatchers.exact());
        }

        Example<User> example = Example.of(u, matcher);
        Sort sort = Sort.by(Sort.Direction.DESC, "registerTime");

        Pageable pageableReq = org.springframework.data.domain.PageRequest.
                of(Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
        Page<User> users = userDao.findAll(example, pageableReq);
        users.getContent().stream().forEach(uu->{
            uu.setPassword(null);
            uu.setSalt(null);
        });
        return PageModel.instance(users.getTotalElements(), users.getContent());
    }

    @Transactional
    @Override
    public User saveOrUpdate(User user) {
        return userDao.save(user);
    }

    public boolean certify(User user) {
        boolean certifiedRes = certifyService.getCertifiedInfo(user.getName(), user.getIdentityID());
        if (certifiedRes) {
            user.setIsCertified(true);
            userDao.save(user);
        }
        return certifiedRes;
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
        setUserSession(user);
    }

    @Override
    public String setUserSession(User persistedUser) {
        SessionInfo session = new SessionInfo();
        session.setLasttimestamp(persistedUser.getLoginTime().toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String token = getToken(persistedUser);
        session.setId(persistedUser.getId());
        session.setIsLocked(persistedUser.getIsLocked());
        CacheUtil.setSessionInfo(token, session);
        return token;
    }

    private String getToken(User persistedUser) {
        return persistedUser.getId() + UUID.randomUUID().toString().replaceAll("-", "");
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
    @Transactional
    public void setAssetPassword(Long userId, String password) {
        if (!StringUtils.isEmpty(password)) {
            User user = findById(userId);
            user.setAssetPassword(password);
            user.encryptAssetPassword();
            saveOrUpdate(user);
        }
    }

    @Override
    public User findById(Long userId) {
        User user =  userDao.findById(userId).orElse(null);
        if (user != null && StringUtils.isEmpty(user.getUserCode())) {
            user.setUserCode(ShareCodeUtil.generateUserCode(user.getId()));
        }
        return user;
    }

    @Override
    public UserMoney findMoneyByUserId(Long userId) {
        UserMoney um = userMoneyDao.findByUserId(userId);
        if (um == null) {
            um = new UserMoney();
        }
        return um;
    }

    @Override
    public PageModel<PayRecord> listMoneyReturnRecord(Long userId,
                                                      PageQueryParamDTO pageRequest,
                                                      Set<MoneyTypeEnum> types) {
        return payRecordService.listMoneyReturnRecord(userId, pageRequest, types);
    }

    @Override
    public UserScore findScoreByUserId(Long userId) {
        UserScore um = userScoreDao.findByUserId(userId);
        if (um == null) {
            um = new UserScore();
        }
        return um;
    }

    @Override
    public PageModel<PayRecord> listScoreReturnRecord(Long userId, PageQueryParamDTO pageRequest) {
        return payRecordService.listScoreReturnRecord(userId, pageRequest);
    }

    @Override
    public void checkUserCertified(User user) {
        if (user.getIsCertified() == null || !user.getIsCertified()) {
            throw BusinessException.build(ErrorCode.USER_NOT_CERTIFIED, "用户未实名认证");
        }
    }

    @Override
    public void checkUserCertified(Long userId) {
        User user = findById(userId);
        if (user == null) {
            throw BusinessException.build("用户不存在");
        }
        checkUserCertified(user);
    }

    @Override
    @Transactional
    public void setAlipayAccount(Long userId, String alipayAccount, String alipayName) {
        User user = findById(userId);
        if (user == null) {
            throw BusinessException.build("用户不存在");
        }
        checkUserCertified(user);
        user.setAlipayAccount(alipayAccount);
        user.setAlipayName(alipayName);
        userDao.save(user);
    }

	@Override
	@Transactional
	public void deleteById(Long userId) {
		userDao.deleteById(userId);
	}
}
