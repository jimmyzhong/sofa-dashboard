package me.izhong.dashboard.web.bean;

import me.izhong.common.constant.ErrCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResponseContainer<T> implements Serializable {

    public static final String SUCCESS_CODE = ErrCode.SUCCESS_CODE;
    public static final String FAIL_CODE = ErrCode.FAIL_CODE;
    private String code;
    private String msg;

    private T data;

    public ResponseContainer() {

    }

    public ResponseContainer(String code, T data) {
        this(code, null, data);
    }

    public ResponseContainer(String code, String msg, T data) {
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

    public static <T> ResponseContainer<T> container(String code, String msg, T data) {
        ResponseContainer<T> rc = new ResponseContainer<>();
        rc.setData(data);
        rc.setMsg(msg);
        rc.setCode(code);
        return rc;
    }


}
