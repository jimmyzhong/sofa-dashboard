package me.izhong.shop.entity;

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
@Table(name = "PLATFORM_PARAM_CONFIG")
public class PlatformParamConfig {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "CONFIG_KEY")
    private String configKey;
    @Column(name = "CONFIG_VALUE")
    private String configValue;
    @Column(name = "DESCRIPTION")
    private String description;
}
