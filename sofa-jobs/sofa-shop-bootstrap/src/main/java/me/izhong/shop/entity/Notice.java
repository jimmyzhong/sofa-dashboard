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
@Table(name = "NOTICE")
public class Notice {

	@Id
    @GeneratedValue
    private Long id;

    /**
     * 公告标题
     */
    @Column(name = "TITLE")
    private String title;

    /**
     * 公告内容
     */
    @Column(name = "CONTENT")
    private String content;

    /**
     * 公告链接
     */
    @Column(name = "LINK")
    private String link;

    /**
     * 状态(1:公布;0:不发布)
     * 
     */
    @Column(name = "STATUS")
    private Integer status;

    /**
     * 是否置顶（1:是;0:否）
     */
    @Column(name = "IS_TOP")
    private Integer isTop;

    /**
     * 创建时间
     */
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
