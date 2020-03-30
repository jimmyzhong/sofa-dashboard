package me.izhong.jobs.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ShopUser implements Serializable {

    private Long id;

    private String password;
    private String loginName;
    private String nickName;
    private String name;
    private String identityID;
    private Boolean isCertified;
    private String avatar;
    private String phone;
    private String email;
    private Boolean isLocked;
    private Long inviteUserId; //上级
    private Long inviteUserId2;//上上级
    private Date registerTime;
    private Date loginTime;

}
