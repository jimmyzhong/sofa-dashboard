package me.izhong.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveAddressParam {

    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String postCode;
    private String province;
    private String city;
    private String district;
    private String town;
    private String detailAddress;
    private Integer isDefault;
}
