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
@Table(name = "APP_VERSIONS")
public class AppVersions {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "TYPE")
    private String type;
    @Column(name = "VERSION")
    private String version;
    @Column(name = "DESC")
    private String desc;
    @Column(name = "URL")
    private String url; 
    @Column(name = "FORCE_UPDATE_VERSION")
    private String forceUpdateVersion;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
