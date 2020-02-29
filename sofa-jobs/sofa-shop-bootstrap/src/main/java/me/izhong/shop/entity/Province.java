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
@Table(name = "PROVINCE")
public class Province {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "PROVINCE_CODE")
	private String provinceCode;
    @Column(name = "PROVINCE_NAME")
	private String provinceName;
}
