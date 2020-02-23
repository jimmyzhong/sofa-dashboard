package me.izhong.shop.filter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("api接口通用返回对象")
public class ResponseContainer<T> implements Serializable {

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 409;

    @ApiModelProperty(value = "返回码",dataType = "int")
    private int code;
    @ApiModelProperty(value = "错误信息",dataType = "String")
    private String msg;

    private T data;

    public ResponseContainer() {

    }

    public ResponseContainer(int code, T data) {
        this(code, null, data);
    }

    public ResponseContainer(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResponseContainer<T> successContainer(T data) {
        return container(SUCCESS_CODE, "成功", data);
    }

    public static <T> ResponseContainer<T> failContainer(String msg) {
        return container(FAIL_CODE, msg, null);
    }

    public static <T> ResponseContainer<T> failContainer(String msg, T data) {
        return container(FAIL_CODE, msg, data);
    }

    public static <T> ResponseContainer<T> container(int code, String msg, T data) {
        ResponseContainer<T> rc = new ResponseContainer<>();
        rc.setData(data);
        rc.setMsg(msg);
        rc.setCode(code);
        return rc;
    }


}
