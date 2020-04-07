package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "LOTS_ITEM")
@SequenceGenerator(name = "LOTS_ITEM_SEQ", sequenceName = "LOTS_ITEM_SEQ", allocationSize = 1)
public class LotsItem {
    @Id
    @GeneratedValue(generator = "LOTS_ITEM_SEQ", strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(name = "LOTS_ID")
    private Long lotsId;

    @Column(name = "SEQ_ID")
    private Long seqId;

    /**
     * 出价用户ID
     */
    @Column(name = "USER_ID")
    private Long userId;

    /**
     * 出价用户
     */
    @Column(name = "USER_NICK")
    private String userNick;

    /**
     * 出价用户ava
     */
    @Column(name = "USER_AVATAR")
    private String userAva;

    /**
     * 出价价格，单位分
     */
    @Column(name = "PRICE")
    private Long price;

    /**
     * 本次出价的收益，单位分
     */
    @Column(name = "OFFER_AMOUNT")
    private Long offerAmount;

    /**
     * 出价时间
     */
    @Column(name = "BID_TIME")
    private LocalDateTime bidTime;
}
