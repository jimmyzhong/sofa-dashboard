package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopReceiverInfo {

    private Long orderId;
    private String receiverName;
    private String receiverPhone;
    private String receiverPostCode;
    private String receiverDetailAddress;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private Integer status;
}
