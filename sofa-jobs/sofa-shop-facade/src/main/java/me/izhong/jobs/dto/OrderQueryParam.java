package me.izhong.jobs.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderQueryParam {

	/**
	 * 订单编号
	 */
    private String orderSn;
    /**
     * 收货人姓名/号码
     */
    private String receiverKeyword;
    /**
     * 订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
     */
    private Integer status;
    /**
     * 订单类型：0->正常订单；1->寄售订单
     */
    private Integer orderType;
    /**
     * 订单来源：0->h5订单；1->app订单
     */
    private Integer sourceType;
    private Long userId;

    private LocalDateTime start;
    private LocalDateTime end;
}
