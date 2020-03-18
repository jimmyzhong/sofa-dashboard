package me.izhong.dashboard.common.expection;

import me.izhong.common.exception.BusinessException;

/**
 * 验证码错误异常类
 */
public class CaptchaException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public CaptchaException() {
        super(400, "验证码错误");
    }
}
