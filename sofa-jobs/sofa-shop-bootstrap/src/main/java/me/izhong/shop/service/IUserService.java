package me.izhong.shop.service;

import java.util.Set;

import me.izhong.common.domain.PageModel;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.PayRecord;
import me.izhong.shop.entity.User;
import me.izhong.shop.entity.UserMoney;
import me.izhong.shop.entity.UserScore;

public interface IUserService {

    PageModel<User> list(Long userId, PageQueryParamDTO request);

    User saveOrUpdate(User user);

    User findById(Long userId);

    void expectNew(User user);

    User registerUser(User user);

    boolean certify(User user);

    User expectExists(User user);

    void lock(Long userId, boolean isLock);

    void attemptModifyPhone(User user, String newPhoneNumber);

    void attemptModifyEmail(User user, String email);

    void attemptModifyLoginName(User dbUser, String loginName);

    void attemptModifyPassword(User dbUser, String password);

    void setAssetPassword(Long userId, String password);

    UserMoney findMoneyByUserId(Long userId);

    PageModel<PayRecord> listMoneyReturnRecord(Long userId, PageQueryParamDTO pageRequest, Set<MoneyTypeEnum> types);

    UserScore findScoreByUserId(Long userId);

    PageModel<PayRecord> listScoreReturnRecord(Long userId, PageQueryParamDTO pageRequest);

    void checkUserCertified(User user);

    void checkUserCertified(Long userId);

    void setAlipayAccount(Long userId, String alipayAccount, String alipayName);

	void deleteById(Long userId);
}
