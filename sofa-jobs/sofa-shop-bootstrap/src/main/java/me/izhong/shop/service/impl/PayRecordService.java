package me.izhong.shop.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.javafx.fxml.expression.Expression;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.PayMethodEnum;
import me.izhong.shop.dao.JobDao;
import me.izhong.shop.dao.PayRecordDao;
import me.izhong.shop.dao.UserMoneyDao;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.*;
import me.izhong.shop.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.izhong.common.util.DateUtil.convertToLocalDate;

@Service
@Slf4j
public class PayRecordService {
    @Autowired private PayRecordDao payRecordDao;
    @Autowired private UserMoneyDao userMoneyDao;
    @Autowired private IUserService userService;
    @Autowired private JobDao jobDao;

    @Transactional
    public void updateUserMoney(Long userId, LocalDateTime start, LocalDateTime end) {
        UserMoney userMoney = userMoneyDao.selectUserForUpdate(userId);
        List<PayRecord> getMoneyRecords = payRecordDao.findAllByReceiverAndBetweenCreationDateAndTypeIn(userId, start, end,
                0, Arrays.asList(MoneyTypeEnum.RETURN_MONEY.getDescription(),
                        MoneyTypeEnum.RESALE_GOODS.getDescription()));
        BigDecimal money = getMoneyRecords.stream().map(p->{
            p.setSysState(1);
            return p.getTotalAmount();
        }).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);


        BigDecimal moneyFromResale = getMoneyRecords.stream().filter(p->MoneyTypeEnum.RESALE_GOODS.getDescription().equalsIgnoreCase(p.getType()))
                .map(PayRecord::getTotalAmount)
                .reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

        userMoney.setAvailableAmount(userMoney.getAvailableAmount().add(money));
        userMoney.setMoneySaleAmount(userMoney.getMoneySaleAmount().add(moneyFromResale));
        userMoney.setMoneyReturnAmount(userMoney.getMoneyReturnAmount().add(money.subtract(moneyFromResale)));
        userMoneyDao.save(userMoney);
        payRecordDao.saveAll(getMoneyRecords);

        log.info("update user money done for " + userId);
    }

    public Set<Long> getUserIdsWhoReceivedMoneyBetween(LocalDateTime start, LocalDateTime end) {
        return payRecordDao.findReceiversBetweenCreationDateWithSysState(start, end, 0);
    }


    public PageModel<PayRecord> listMoneyReturnRecord(Long userId,
                                                      PageRequest pageRequest, Set<MoneyTypeEnum> types) {
        LocalDate start = convertToLocalDate(pageRequest.getBeginCreateTime());
        LocalDate end = convertToLocalDate(pageRequest.getEndCreateTime());
        Integer state = null;
        if (!StringUtils.isEmpty(pageRequest.getStatus())) {
            try {
                state = Integer.valueOf(pageRequest.getStatus());
            }catch (Exception e) {
                throw BusinessException.build("状态码请传递'1'或'0'");
            }
        }
        Specification<PayRecord> specification = getMoneyReturnQuery(userId, state, start, end, types );

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        return getPayRecordPageModel(pageRequest, specification, sort);
    }

    public PageModel<PayRecord> listScoreReturnRecord(Long userId,
    												  PageRequest pageRequest) {
        LocalDate start = convertToLocalDate(pageRequest.getBeginCreateTime());
        LocalDate end = convertToLocalDate(pageRequest.getEndCreateTime());
        Integer state = null;
        if (!StringUtils.isEmpty(pageRequest.getStatus())) {
            try {
                state = Integer.valueOf(pageRequest.getStatus());
            }catch (Exception e) {
                throw BusinessException.build("状态码请传递'1'或'0'");
            }
        }
        Specification<PayRecord> specification = getScoreReturnQuery(userId, state, start, end);

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

    private Specification<PayRecord> getMoneyReturnQuery(Long userId, Integer state, LocalDate start, LocalDate end, Set<MoneyTypeEnum> types) {
        return (r, q, cb) -> {
            Predicate predicateOfReceive = cb.equal(r.get(PayRecord_.receiverId), userId);
             if (types != null && !types.isEmpty()) {
                 predicateOfReceive = cb.and(predicateOfReceive, r.get(PayRecord_.type).in(types.stream()
                         .map(MoneyTypeEnum::getDescription).collect(Collectors.toList())));
             } else {
                 predicateOfReceive = cb.and(predicateOfReceive, r.get(PayRecord_.type).in(Stream.of(MoneyTypeEnum.RESALE_GOODS,
                         MoneyTypeEnum.RETURN_MONEY, MoneyTypeEnum.DEPOSIT_MONEY)
                         .map(MoneyTypeEnum::getDescription).collect(Collectors.toList())));
             }

             Predicate predicateOfSpend = cb.and(cb.equal(r.get(PayRecord_.payerId), userId),
                     cb.equal(r.get(PayRecord_.payMethod), PayMethodEnum.MONEY.name()));

             if (state != null) {
                 if (state == 1) {
                     predicateOfReceive = cb.and(predicateOfReceive, cb.equal(r.get(PayRecord_.sysState), 1));
                 } else if (state ==0){
                     predicateOfReceive = cb.and(predicateOfReceive, cb.equal(r.get(PayRecord_.sysState), 0));
                 }
             }

            Predicate predicate = cb.or(predicateOfReceive, predicateOfSpend);
            predicate = getCreatedTimeBetweenQuery(start, end, r, cb, predicate);
            return predicate;
            };
    }

    private Specification<PayRecord> getScoreReturnQuery(Long userId, Integer state, LocalDate start, LocalDate end) {
        return (r, q, cb) -> {
            Predicate predicateOfReceive = cb.and(cb.equal(r.get(PayRecord_.receiverId), userId),
                    cb.or(cb.equal(r.get(PayRecord_.type), MoneyTypeEnum.RETURN_SCORE.getDescription()),
                            cb.equal(r.get(PayRecord_.type), MoneyTypeEnum.GIFT_SCORE.getDescription())));

            Predicate predicateOfSpend = cb.and(cb.equal(r.get(PayRecord_.payerId), userId),
                    cb.equal(r.get(PayRecord_.payMethod), PayMethodEnum.SCORE.name()));

            if (state != null) {
                if (state == 1) { //
                    predicateOfReceive = cb.and(predicateOfReceive, cb.equal(r.get(PayRecord_.sysState), 1));
                } else if (state == 0) {
                    predicateOfReceive = cb.and(predicateOfReceive, cb.equal(r.get(PayRecord_.sysState), 0));
                }
            }

            Predicate predicate = cb.or(predicateOfReceive, predicateOfSpend);
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
