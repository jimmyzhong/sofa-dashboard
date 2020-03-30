package me.izhong.jobs.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopLotsCategory implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
    private String name;
    private String logo;
    private String password;
    private Long admin;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
