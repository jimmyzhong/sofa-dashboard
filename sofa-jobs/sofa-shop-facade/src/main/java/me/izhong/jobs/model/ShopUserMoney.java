package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopUserMoney implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private Long userId;
    private BigDecimal availableAmount = BigDecimal.ZERO;
    private BigDecimal unavailableAmount = BigDecimal.ZERO;
    private BigDecimal moneyReturnAmount = BigDecimal.ZERO;
    private BigDecimal moneyDepositAmount = BigDecimal.ZERO;
    private BigDecimal moneySaleAmount = BigDecimal.ZERO;
}
