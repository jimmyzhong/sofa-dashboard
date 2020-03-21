package me.izhong.shop.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.dao.PayRecordDao;
import me.izhong.shop.dao.UserMoneyDao;
import me.izhong.shop.entity.PayRecord;
import me.izhong.shop.entity.PayRecord_;
import me.izhong.shop.entity.User;
import me.izhong.shop.entity.UserMoney;
import me.izhong.shop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static me.izhong.common.util.DateUtil.convertToLocalDate;

@Service
@Slf4j
public class PayRecordService {
    @Autowired private PayRecordDao payRecordDao;
    @Autowired private UserMoneyDao userMoneyDao;
    @Autowired private IUserService userService;

    @PostConstruct
    public void setUp() {
        ScheduledExecutorService userMoneyUpdater = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("user-money-updater").build());
        userMoneyUpdater.scheduleAtFixedRate(() -> {
            // updateUserMoneyJob();
        }, 1, 1, TimeUnit.DAYS);
    }

    private void updateUserMoneyJob() {
        try {
            LocalDateTime now = LocalDateTime.now(); // TODO get last run time
            LocalDateTime start = LocalDateTime.of(now.minusDays(1).toLocalDate(), LocalTime.of(23,00,00));
            LocalDateTime end = LocalDateTime.of(now.toLocalDate(), LocalTime.of(23,00,00));
            if (end.isAfter(now)) {
                end = now;
            }

            Set<Long> userIds = getUserIdsWhoReceivedMoneyBetween(start, end);
            userIds.addAll(getUserIdsWhoWithdrawMoneyBetween(start, end));

            for (Long userId: userIds) {
                updateUserMoney(userId, start, end);
            }
        }catch (Throwable throwable) {
            log.error("order update expired status error", throwable);
        }
    }

    @Transactional
    public void updateUserMoney(Long userId, LocalDateTime start, LocalDateTime end) {
        List<PayRecord> getMoneyRecords = payRecordDao.findAllByReceiverAndBetweenCreationDateAndTypeIn(userId, start, end,
                0, Arrays.asList(MoneyTypeEnum.RETURN_MONEY.getDescription()));
        BigDecimal money = getMoneyRecords.stream().map(p->{
            p.setSysState(1);
            return p.getTotalAmount();
        }).reduce((a, b) -> a.add(b)).get();

        List<PayRecord> withdrawMoneyRecords = payRecordDao.findAllByPayerIdAndBetweenCreationDateAndTypeIn(userId, start, end,
                0, Arrays.asList(MoneyTypeEnum.WITHDRAW_MONEY.getDescription()));
        BigDecimal withDrawMoney = getMoneyRecords.stream().map(p->{
            p.setSysState(1);
            return p.getTotalAmount();
        }).reduce((a, b) -> a.add(b)).get();

        UserMoney userMoney = userMoneyDao.selectUserForUpdate(userId);
        userMoney.setAvailableAmount(userMoney.getAvailableAmount().add(money));
        userMoney.setUnavailableAmount(userMoney.getUnavailableAmount().subtract(withDrawMoney));
        userMoneyDao.save(userMoney);
        payRecordDao.saveAll(getMoneyRecords);
        payRecordDao.saveAll(withdrawMoneyRecords);

        log.info("update user money done for " + userId);
    }

    @Transactional
    public void addWithdrawMoneyRecord(Long userId, BigDecimal amount, String alipayAccount) {
        UserMoney userMoney = userMoneyDao.selectUserForUpdate(userId);
        if (userMoney == null) {
            throw BusinessException.build("用户余额不存在,请联系管理员");
        }

        if (userMoney.getAvailableAmount().compareTo(amount) < 0) {
            throw BusinessException.build("可用余额不足");
        }
        userMoney.setAvailableAmount(userMoney.getAvailableAmount().subtract(amount));
        userMoney.setUnavailableAmount(userMoney.getUnavailableAmount().add(amount));

        PayRecord moneyWithdraw = new PayRecord();
        moneyWithdraw.setType(MoneyTypeEnum.WITHDRAW_MONEY.getDescription());
        moneyWithdraw.setCreateTime(LocalDateTime.now());
        moneyWithdraw.setPayerId(userId);
        moneyWithdraw.setTotalAmount(amount);
        moneyWithdraw.setSysState(-1);
        moneyWithdraw.setAccount(alipayAccount);
        payRecordDao.save(moneyWithdraw);
    }

    private Set<Long> getUserIdsWhoReceivedMoneyBetween(LocalDateTime start, LocalDateTime end) {
        return payRecordDao.findReceiversBetweenCreationDateWithSysState(start, end, 0);
    }

    private Set<Long> getUserIdsWhoWithdrawMoneyBetween(LocalDateTime start, LocalDateTime end) {
        return payRecordDao.findPayersBetweenCreationDateWithSysStateAndTypeIn(start, end, 0,
                Arrays.asList(MoneyTypeEnum.WITHDRAW_MONEY.getDescription()));
    }

    public PageModel<PayRecord> listMoneyReturnRecord(Long userId,
                                                      PageRequest pageRequest) {
        LocalDate start = convertToLocalDate(pageRequest.getBeginCreateTime());
        LocalDate end = convertToLocalDate(pageRequest.getEndCreateTime());
        Specification<PayRecord> specification = getMoneyReturnQuery(userId, start, end);

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        return getPayRecordPageModel(pageRequest, specification, sort);
    }

    public PageModel<PayRecord> listScoreReturnRecord(Long userId,
                                                      PageRequest pageRequest) {
        LocalDate start = convertToLocalDate(pageRequest.getBeginCreateTime());
        LocalDate end = convertToLocalDate(pageRequest.getEndCreateTime());
        Specification<PayRecord> specification = getScoreReturnQuery(userId, start, end);

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        return getPayRecordPageModel(pageRequest, specification, sort);
    }

    private PageModel<PayRecord> getPayRecordPageModel(PageRequest pageRequest, Specification<PayRecord> specification, Sort sort) {
        Pageable pageableReq = org.springframework.data.domain.PageRequest.
                of(Long.valueOf(pageRequest.getPageNum()-1).intValue(),
                        Long.valueOf(pageRequest.getPageSize()).intValue(), sort);

        Page<PayRecord> page = payRecordDao.findAll(specification, pageableReq);
        return PageModel.instance(page.getTotalElements(), page.getContent());
    }

    private Specification<PayRecord> getMoneyReturnQuery(Long userId, LocalDate start, LocalDate end) {
        return (r, q, cb) -> {
                Predicate predicate = cb.and(cb.equal(r.get(PayRecord_.receiverId), userId),
                        cb.or(cb.equal(r.get(PayRecord_.type), MoneyTypeEnum.RETURN_MONEY.getDescription()),
                                cb.equal(r.get(PayRecord_.type), MoneyTypeEnum.RESALE_GOODS.getDescription())));
            predicate = getCreatedTimeBetweenQuery(start, end, r, cb, predicate);

            return predicate;
            };
    }

    private Specification<PayRecord> getScoreReturnQuery(Long userId, LocalDate start, LocalDate end) {
        return (r, q, cb) -> {
            Predicate predicate = cb.and(cb.equal(r.get(PayRecord_.receiverId), userId),
                    cb.equal(r.get(PayRecord_.type), MoneyTypeEnum.RETURN_SCORE.getDescription()));
            predicate = getCreatedTimeBetweenQuery(start, end, r, cb, predicate);

            return predicate;
        };
    }

    private Predicate getCreatedTimeBetweenQuery(LocalDate start, LocalDate end, Root<PayRecord> r, CriteriaBuilder cb, Predicate predicate) {
        if (start != null) {
            predicate = cb.and(predicate, cb.greaterThan(r.get(PayRecord_.createTime), start.atStartOfDay()));
        }
        if (end != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(r.get(PayRecord_.createTime), end.atStartOfDay()));
        }
        return predicate;
    }
}
