package me.izhong.shop.entity;

import java.time.LocalDateTime;

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
@Table(name = "CONSIGNMENT_RULE")
public class ConsignmentRule {

	@Id
    @GeneratedValue
	private Long id;
    @Column(name = "RULE_NO")
	private String ruleNo;
    @Column(name = "LIMIT_RULE")
	private String limitRule;
    @Column(name = "BEGIN_TIME")
	private LocalDateTime beginTime;
    @Column(name = "END_TIME")
	private LocalDateTime endTime;
    @Column(name = "IS_DELETE")
	private Integer isDelete;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
