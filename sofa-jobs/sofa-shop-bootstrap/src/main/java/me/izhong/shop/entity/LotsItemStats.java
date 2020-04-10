package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户纬度拍卖统计
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "LOTS_ITEM_STATS")
@SequenceGenerator(name = "LOTS_ITEM_STATS_SEQ", sequenceName = "LOTS_ITEM_STATS_SEQ", allocationSize = 1)
public class LotsItemStats {
    @Id
    @GeneratedValue(generator = "LOTS_ITEM_STATS_SEQ", strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(name = "LOTS_ID")
    private Long lotsId;

    /**
     * 出价用户ID
     */
    @Column(name = "USER_ID")
    private Long userId;

    /**
     * 出价用户
     */
    @Column(name = "USER_NICK", length = 20)
    private String userNick;

    /**
     * 出价用户ava
     */
    @Column(name = "USER_AVATAR", length = 200)
    private String userAva;
    /**
     * 出价次数
     */
    @Column(name = "TIMES")
    private Integer times;

    /**
     * 累计加价金额
     */
    @Column(name = "AMOUNT")
    private BigDecimal amount;

    /**
     * 加价收益
     */
    @Column(name = "OFFER_AMOUNT")
    private BigDecimal offerAmount;
}
