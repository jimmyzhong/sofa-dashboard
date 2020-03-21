package me.izhong.shop.consts;

import me.izhong.common.exception.BusinessException;

import java.util.Arrays;

public enum MoneyTypeEnum {
    UNKNOWN (-1, "未知类型"),
    // 订单类型和付款类型
    NORMAL_GOODS (0, "普通商品"),
    DEPOSIT_MONEY (1, "充值余额"),
    RESALE_GOODS (2, "寄售商品"),
    // 仅付款类型
    RETURN_MONEY (10, "返现"),
    RETURN_SCORE (11, "返积分"),
    SPEND_MONEY (12, "余额付款"),
    SPEND_SCORE (13, "积分兑换"),
    WITHDRAW_MONEY (14, "提现"),;

    int type;
    String description;

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    MoneyTypeEnum(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public static String getDescriptionByState(int type) {
        return Arrays.stream(MoneyTypeEnum.values()).filter(e->e.type==type).findFirst()
                .orElseThrow(()-> BusinessException.build("")).description;
    }

    public static MoneyTypeEnum getByType(int type) {
        return Arrays.stream(MoneyTypeEnum.values()).filter(e->e.type==type).findFirst()
                .orElse(null);
    }

    public static int getTypeByDescription(String description) {
        return Arrays.stream(MoneyTypeEnum.values()).filter(e->e.description.equalsIgnoreCase(description)).findFirst()
                .orElse(UNKNOWN).type;
    }
}
