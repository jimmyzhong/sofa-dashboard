package me.izhong.shop.response;

public enum ResponseCode {
    USER_NOT_LOGIN(10, "User not Logged in"),
    TOKEN_EXPIRED(1, "Token Expired"),
    TOKEN_INVALID(2, "Token Invalid"),
    SIGN_INVALID(3, "Signature Invalid");
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    int code;
    String message;

    public String getDescription() {
        return code + ":" + message;
    }
}
