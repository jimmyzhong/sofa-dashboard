package me.izhong.jobs.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopPlatformParamConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
}
