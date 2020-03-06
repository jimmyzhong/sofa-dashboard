package me.izhong.shop.dto.order;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SubmitOrderResponse {
    String orderNo;
    String status;
    String timeToPay;
}
