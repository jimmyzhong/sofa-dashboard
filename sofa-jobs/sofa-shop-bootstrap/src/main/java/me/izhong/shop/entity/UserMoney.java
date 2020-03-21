package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "USER_MONEY")
@SequenceGenerator(name = "USER_MONEY_SEQ", sequenceName = "USER_MONEY_SEQ", allocationSize = 1)
/**
 *  用户余额
 */
public class UserMoney extends EditableEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "AVA_AMOUNT")
    private BigDecimal availableAmount = BigDecimal.ZERO;
    @Column(name = "UNAVA_AMOUNT")
    private BigDecimal unavailableAmount = BigDecimal.ZERO;
    @Column(name = "HIS_AMOUNT_MONEY_RETURN")
    private BigDecimal moneyReturnAmount = BigDecimal.ZERO;
    @Column(name = "HIS_AMOUNT_MONEY_DEPOSIT")
    private BigDecimal moneyDepositAmount = BigDecimal.ZERO;
    @Column(name = "HIS_AMOUNT_SALE")
    private BigDecimal moneySaleAmount = BigDecimal.ZERO;
    @Column(name = "USER_ID")
    private Long userId;
}
