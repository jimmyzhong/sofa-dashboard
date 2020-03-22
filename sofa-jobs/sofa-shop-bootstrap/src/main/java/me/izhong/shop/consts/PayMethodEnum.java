package me.izhong.shop.consts;

public enum PayMethodEnum {
    ALIPAY(0),
    WECHAT(1),
    CARD(2),
    MONEY(3), // 余额
    SCORE(4); // 积分
    int code;
    PayMethodEnum(int code){
        this.code  = code;
    }

    public int getCode() {
        return code;
    }
}
