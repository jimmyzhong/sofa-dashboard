package me.izhong.shop.dto;

import lombok.Data;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;

import java.util.List;

@Data
public class PageQueryParamDTO extends PageRequest {
    //common
    private String code;
    private String query;
    //good
    private String categoryPath;
    private Integer productType;
    private Long userId;
    private Boolean onIndexPage;
    // user
    private Long inviteUserId;
    private Long inviteUserId2;
    // 余额类型
    private List<Integer> moneyTypes; // 0:普通商品,1:充值,2:寄售商品,10:返现

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
