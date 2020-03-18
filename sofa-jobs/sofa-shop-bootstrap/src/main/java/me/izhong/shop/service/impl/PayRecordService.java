package me.izhong.shop.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.dao.PayRecordDao;
import me.izhong.shop.dao.UserMoneyDao;
import me.izhong.shop.entity.PayRecord;
import me.izhong.shop.entity.PayRecord_;
import me.izhong.shop.entity.UserMoney;
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
            for (Long userId: userIds) {
                updateUserMoney(userId, start, end);
            }
        }catch (Throwable throwable) {
            log.error("order update expired status error", throwable);
        }
    }

    @Transactional
    public void updateUserMoney(Long userId, LocalDateTime start, LocalDateTime end) {
        List<PayRecord> payRecords = payRecordDao.findAllByReceiverAndBetweenCreationDate(userId, start, end, 0);
        BigDecimal money = payRecords.stream().map(p->{
            p.setSysState(1);
            return p.getTotalAmount();
        }).reduce((a, b) -> a.add(b)).get();

        UserMoney userMoney = userMoneyDao.selectUserForUpdate(userId);
        userMoney.setAvailableAmount(userMoney.getAvailableAmount().add(money));
        userMoneyDao.save(userMoney);
        payRecordDao.saveAll(payRecords);
        log.info("update user money done for " + userId);
    }

    private Set<Long> getUserIdsWhoReceivedMoneyBetween(LocalDateTime start, LocalDateTime end) {
        return payRecordDao.findReceiversBetweenCreationDateWithSysState(start, end, 0);
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
