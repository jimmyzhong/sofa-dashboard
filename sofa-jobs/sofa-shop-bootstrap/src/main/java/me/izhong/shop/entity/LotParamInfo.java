package me.izhong.shop.entity;

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
@Table(name = "LOT_PARAM_INFO")
public class LotParamInfo {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "TYPE")
    private Integer type;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "RADIX")
    private String radix;
    @Column(name = "BALANCE_REWARD")
    private String balanceReward;
    @Column(name = "POINT_REWARD")
    private String pointReward;
    @Column(name = "DESCRIPTION")
    private String description;
}
