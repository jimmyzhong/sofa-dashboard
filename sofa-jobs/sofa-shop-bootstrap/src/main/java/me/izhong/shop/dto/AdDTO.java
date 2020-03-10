package me.izhong.shop.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdDTO {

    private Long id;
    private String adName;
    private String adLink;
    private Integer adType;
    private String imageUrl; 
    private Integer position;
    private String content;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
