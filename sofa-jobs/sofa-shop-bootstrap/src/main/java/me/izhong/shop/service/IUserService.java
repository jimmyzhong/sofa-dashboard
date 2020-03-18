package me.izhong.shop.service;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.shop.entity.PayRecord;
import me.izhong.shop.entity.User;
import me.izhong.shop.entity.UserMoney;
import me.izhong.shop.entity.UserScore;

import java.time.LocalDate;

public interface IUserService {

    PageModel<User> list(Long userId, me.izhong.common.domain.PageRequest request);

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

    UserMoney findMoneyByUserId(Long userId);

    PageModel<PayRecord> listMoneyReturnRecord(Long userId, PageRequest pageRequest);

    UserScore findScoreByUserId(Long userId);

    PageModel<PayRecord> listScoreReturnRecord(Long userId, PageRequest pageRequest);
}
