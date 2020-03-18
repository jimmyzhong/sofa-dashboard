package me.izhong.dashboard.common.constants;

import lombok.Getter;

public enum OperatorType {
    /**
     * 其它
     */
    OTHER(0,"其它"),

    /**
     * 后台用户
     */
    MANAGE(1,"后台用户"),

    /**
     * 手机端用户
     */
    MOBILE(2,"手机端用户");

    @Getter
    private Integer index;

    @Getter
    private String title;

    OperatorType(Integer index, String title){
        this.index = index;
        this.title = title;
    }
}
