package me.izhong.common.exception;

import me.izhong.common.constant.ErrCode;

public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException() {

    }

    public BusinessException(int code, String msg) {
        this(code, msg, null);
    }

    public BusinessException(int code, String msg, Exception e) {
        super(msg, e);
        this.code = code;
    }

    public static BusinessException build(String msg) {
        return new BusinessException(ErrCode.FAIL_CODE, msg);
    }

    public static BusinessException build(String msg, Exception e) {
        return new BusinessException(ErrCode.FAIL_CODE, msg, e);
    }

    public static BusinessException build(int code, String msg) {
        return new BusinessException(code, msg);
    }

    public static BusinessException build(int code, String msg, Exception e) {
        return new BusinessException(code, msg, e);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
