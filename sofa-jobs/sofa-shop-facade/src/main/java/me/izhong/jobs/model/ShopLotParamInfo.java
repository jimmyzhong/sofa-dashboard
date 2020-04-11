package me.izhong.jobs.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopLotParamInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private Long id;
    private Integer type;
    private String title;
    private String radix;
    private String balanceReward;
    private String pointReward;
    private String description;
}
