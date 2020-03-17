package me.izhong.jobs.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopTemplate {

    private Long id;
    private String title;
    private Integer type;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
