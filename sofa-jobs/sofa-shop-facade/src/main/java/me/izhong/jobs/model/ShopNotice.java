package me.izhong.jobs.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopNotice {

    private Long id;
    private String title;
    private String content;
    private Integer status;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
