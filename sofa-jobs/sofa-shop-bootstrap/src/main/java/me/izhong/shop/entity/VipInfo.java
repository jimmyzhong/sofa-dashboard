package me.izhong.shop.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "VIP_INFO")
public class VipInfo {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "LEVEL")
    private String level;
    @Column(name = "NAME")
    private String name;
    @Column(name = "PAY_AMT")
    private BigDecimal payAmt;
    @Column(name = "GIFT_POINTS")
    private Integer giftPoints;
}
