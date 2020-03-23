package me.izhong.jobs.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopAppVersions implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private String type;
    private String version;
    private String desc;
    private String url; 
    private String forceUpdateVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
