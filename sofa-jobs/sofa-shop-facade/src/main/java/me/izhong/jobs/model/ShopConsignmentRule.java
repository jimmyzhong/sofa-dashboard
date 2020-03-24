package me.izhong.jobs.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopConsignmentRule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String ruleNo;
	private String limitRule;
	private String reduceValue;
	private LocalDateTime beginTime;
	private LocalDateTime endTime;
	private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
