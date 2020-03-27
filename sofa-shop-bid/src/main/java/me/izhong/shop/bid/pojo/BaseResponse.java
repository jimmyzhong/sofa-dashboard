package me.izhong.shop.bid.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Setter
@Getter
@ToString
abstract public class BaseResponse implements IResponse {

    @JSONField(serialize = false)
    private int code;

    @JSONField(serialize = false)
    private String msg;

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
