package me.izhong.shop.dto;

import lombok.Data;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;

@Data
public class PageQueryParamDTO extends PageRequest {
    private String code;
    private String query;
    private String categoryPath;
    private Integer productType;
    private Long userId;

    public boolean validRequest() {
        if (this.getPageNum()<0) {
            throw BusinessException.build("请求页码应该大于0");
        }
        if (this.getPageSize() <= 0) {
            throw BusinessException.build("请求页大小应该大于0");
        }
        return true;
    }
}
