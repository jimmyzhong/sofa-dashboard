package me.izhong.shop.response.ali;

import lombok.Data;

/**
 * {
 * 	"Message":"OK",
 * 	"RequestId":"2184201F-BFB3-446B-B1F2-C746B7BF0657",
 * 	"BizId":"197703245997295588^0",
 * 	"Code":"OK"
 * }
 */
@Data
public class SmsResponse {
    private String Code;
    private String Message;
    private String RequestId;
    private String BizId;

    public boolean isSuccess() {
        return "OK".equalsIgnoreCase(Code);
    }
}
