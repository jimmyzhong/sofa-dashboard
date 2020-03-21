package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "PAY_RECORD")
@SequenceGenerator(name = "PAY_RECORD_SEQ", sequenceName = "PAY_RECORD_SEQ", allocationSize = 1)
public class PayRecord{
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;
    @Column(name = "PAY_AMOUNT")
    private BigDecimal payAmount;
    @Column(name = "INTERNAL_ORDER_ID")
    private String internalId;
    @Column(name = "EXTERNAL_ORDER_ID")
    private String externalId;
    @Column(name = "PAY_METHOD", length = 20)
    private String payMethod;
    @Column(name = "TYPE", length = 20)
    private String type;
    @Column(name = "PAY_STATE", length = 20)
    private String payState;
    @Column(name = "SYS_STATE")
    private Integer sysState; // -1:需要后台系统处理; 0: 需要API JOB处理; 1: 已处理
    @Column(name = "COMMENT", length = 200)
    private String comment;
    @Column(name = "PAYER")
    Long payerId;
    @Column(name = "RECEIVER")
    Long receiverId;
    @Column(name="CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name="CREATE_BY")
    private String createdBy;

}
