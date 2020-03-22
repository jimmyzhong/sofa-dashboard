package me.izhong.shop.bid.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Setter
@Getter
@ToString
abstract public class BaseResponse implements IResponse {

    private int code;

    private String message;

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
