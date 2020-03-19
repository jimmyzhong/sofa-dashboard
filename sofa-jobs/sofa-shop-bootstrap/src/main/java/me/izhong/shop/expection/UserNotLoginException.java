package me.izhong.shop.expection;

import me.izhong.common.exception.BusinessException;
import me.izhong.shop.consts.ErrorCode;

public class UserNotLoginException extends BusinessException {
    public UserNotLoginException(){
        super(ErrorCode.USER_NOT_LOGIN,"用户没有登陆，请先登陆");
    }

    public UserNotLoginException(String msg){
        super(ErrorCode.USER_NOT_LOGIN,msg);
    }
}
