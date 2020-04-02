package me.izhong.shop.consts;

import me.izhong.common.exception.BusinessException;

import java.util.Arrays;

public enum LotsStatusEnum {
    NA(-1, "N/A"),
    DEAL(0, "成交"),
    NOT_DEAL(1, "流拍"),
    WAIT_PAY(2, "待付尾款"),
    DONE(3, "结束");

    int type;
    String msg;
    LotsStatusEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public static String getMsgByType(int state) {
        return Arrays.stream(LotsStatusEnum.values()).filter(e->e.type==state).findFirst()
                .orElseThrow(()-> BusinessException.build("")).msg;
    }

    public static int getTypeByMsg(String comment) {
        return Arrays.stream(LotsStatusEnum.values()).filter(e->e.msg.equalsIgnoreCase(comment)).findFirst()
                .orElse(NA).type;
    }
}

