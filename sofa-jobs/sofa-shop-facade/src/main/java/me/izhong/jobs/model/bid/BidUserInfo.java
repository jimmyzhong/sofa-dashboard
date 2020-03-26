package me.izhong.jobs.model.bid;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BidUserInfo {

    private Long userId;

    private String avatar;

    /**
     * 客户端展示名称，给nickName
     */
    private String nickName;

}
