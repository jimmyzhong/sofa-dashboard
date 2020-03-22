package me.izhong.shop.bid.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
abstract public class BaseRequest<T extends BaseResponse> implements IRequest {

    private String traceId;

    private String msgId;

    private String _routeMsgId;

    private String msgSrc;

    private String msgType;

    private String sign;

    abstract public Class<T> responseClass();

}
