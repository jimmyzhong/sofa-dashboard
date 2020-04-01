package me.izhong.jobs.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopSuppliers implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private String name;
    private String logo;
    private String description;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
