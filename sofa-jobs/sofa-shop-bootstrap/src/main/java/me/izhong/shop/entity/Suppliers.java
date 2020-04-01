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
@Table(name = "SUPPLIERS")
public class Suppliers {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "LOGO")
    private String logo;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "CONTENT")
    private String content;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
