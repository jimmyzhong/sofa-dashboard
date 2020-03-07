package me.izhong.shop.consts;

import me.izhong.common.exception.BusinessException;

import java.util.Arrays;

public enum OrderStateEnum {
    WAIT_PAYING(0, "等待支付"),
    PAID(1, "已支付"),
    EXPIRED(2,"超时未支付"),
    CONFIRMED(3, "确认收货"),
    CANCELED(4, "用户取消");

    int state;
    String comment;
    OrderStateEnum(int s, String comment){
        this.state = s;
        this.comment = comment;
    }

    public int getState() {
        return state;
    }

    public String getComment() {
        return comment;
    }

    public static String getCommentByState(int state) {
        return Arrays.stream(OrderStateEnum.values()).filter(e->e.state==state).findFirst()
                .orElseThrow(()-> BusinessException.build("")).comment;
    }
}
