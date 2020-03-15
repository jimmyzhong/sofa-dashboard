package me.izhong.jobs.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    /**
     * 订单提交时间
     */
    private String createTime;
}
