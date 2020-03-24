package me.izhong.jobs.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopUserScore implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private Long userId;
    private Long availableScore;
    private Long unavailableScore;
}
