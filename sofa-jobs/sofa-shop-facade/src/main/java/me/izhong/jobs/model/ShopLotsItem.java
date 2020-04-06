package me.izhong.jobs.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopLotsItem implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
    private Long lotsId;
    private Long seqId;
    /**
     * 出价用户ID
     */
    private Long userId;
    /**
     * 出价用户
     */
    private String userNick;
    /**
     * 出价用户ava
     */
    private String userAva;
    /**
     * 出价价格，单位分
     */
    private Long price;

    /**
     * 出价时间
     */
    private LocalDateTime bidTime;
}
