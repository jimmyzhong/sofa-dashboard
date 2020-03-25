package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopPayRecord implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String internalId;
    private String externalId;
    private String payMethod;
    private String type;
    private String payState;
    private Integer sysState; // -1:需要后台系统处理; 0: 需要API JOB处理; 1: 已处理
    private String comment;
    private Long payerId;
    private String payerNickName;
    private Long receiverId;
    private String receiverNickName;
    private LocalDateTime createTime;
    private String createdBy;
    private String account;
}
