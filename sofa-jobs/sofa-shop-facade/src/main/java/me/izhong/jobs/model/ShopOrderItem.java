package me.izhong.jobs.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopOrderItem implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private Long userId;
    private Long productId;
    private String productPic;
    private String name;
    private Long orderId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal unitPrice;
    private BigDecimal paidPrice;
    private Integer scoreRedeem;
}
