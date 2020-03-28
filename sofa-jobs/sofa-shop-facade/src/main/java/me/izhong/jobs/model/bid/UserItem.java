package me.izhong.jobs.model.bid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserItem {

    private Long userId;

    private String avatar;

    /**
     * 客户端展示名称，给nickName
     */
    private String nickName;

}
