package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopVipInfo implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private String level;
    private String name;
    private BigDecimal payAmt;
    private Integer giftPoints;
}
