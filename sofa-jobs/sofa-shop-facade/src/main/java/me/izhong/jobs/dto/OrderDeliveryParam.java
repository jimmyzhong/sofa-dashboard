package me.izhong.jobs.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDeliveryParam {

	/**
	 * 订单id
	 */
    private Long orderId;
    /**
     * 物流公司
     */
    private String deliveryCompany;
    /**
     * 物流单号
     */
    private String deliverySn;
}
