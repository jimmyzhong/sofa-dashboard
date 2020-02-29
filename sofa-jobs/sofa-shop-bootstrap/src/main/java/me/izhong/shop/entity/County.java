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
@Table(name = "COUNTY")
public class County {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "CITY_CODE")
	private String cityCode;
    @Column(name = "COUNTY_CODE")
	private String countyCode;
    @Column(name = "COUNTY_NAME")
	private String countyName;
}
