package me.izhong.shop.consts;

import me.izhong.common.exception.BusinessException;

import java.util.Arrays;

public enum  ProductTypeEnum {
    NORMAL(0, "普通商品"),
    RESALE(1, "寄售商品");

    int type;
    String msg;
    ProductTypeEnum(int type, String msg) {
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
        return Arrays.stream(ProductTypeEnum.values()).filter(e->e.type==state).findFirst()
                .orElseThrow(()-> BusinessException.build("")).msg;
    }

    public static int getTypeByMsg(String comment) {
        return Arrays.stream(ProductTypeEnum.values()).filter(e->e.msg.equalsIgnoreCase(comment)).findFirst()
                .orElse(NORMAL).type;
    }
}
