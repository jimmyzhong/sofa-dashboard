package me.izhong.shop.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "AD")
public class Ad {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "AD_NAME")
    private String adName;
    @Column(name = "AD_LINK")
    private String adLink;
    @Column(name = "IMAGE_URL")
    private String imageUrl; 
    @Column(name = "POSITION")
    private Integer position;
    @Column(name = "CONTENT")
    private String content;
    @Column(name = "STATUS")
    private Integer status;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
