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
@Table(name = "USER_RECEIVE_ADDRESS")
public class UserReceiveAddress {

	@Id
    @GeneratedValue
    private Long id;
    @Column(name = "USER_ID")
    private Long userId;
    @Column(name = "USER_NAME")
	private String userName;
    @Column(name = "USER_PHONE")
	private String userPhone;
    @Column(name = "POST_CODE")
	private String postCode;
    @Column(name = "PROVINCE")
	private String province;
    @Column(name = "CITY")
	private String city;
    @Column(name = "DISTRICT")
	private String district;
    @Column(name = "DETAIL_ADDRESS")
	private String detailAddress;
    @Column(name = "IS_DEFAULT")
	private Integer isDefault;
    @Column(name = "CREATE_TIME")
	private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
	private LocalDateTime updateTime;
}
