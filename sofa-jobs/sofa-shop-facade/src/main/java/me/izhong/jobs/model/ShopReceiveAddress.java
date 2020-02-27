package me.izhong.jobs.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopReceiveAddress implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long userId;
	private String name;
	private String phone;
	private String postCode;
	private String province;
	private String city;
	private String region;
	private String detailAddress;
	private Integer isDefault;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}
