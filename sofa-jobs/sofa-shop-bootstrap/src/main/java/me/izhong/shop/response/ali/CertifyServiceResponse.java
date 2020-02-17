package me.izhong.shop.response.ali;

import lombok.Data;

import java.util.Map;

/**
 * {
 *   "code": "10000",
 *   "message": "成功",
 *   "data": {
 *     "result": "1"
 *   },
 *   "seqNo": "4XU29Z4D1704061618"
 * }
 */
@Data
public class CertifyServiceResponse {
    private String code;
    private String message;
    private String seqNo;
    private Map<String, String> data;

    public boolean isSuccess() {
        return "10000".equals(code);
    }

    public String getResult() {
        if (data != null) {
            return data.get("result");
        }
        return null;
    }
}
