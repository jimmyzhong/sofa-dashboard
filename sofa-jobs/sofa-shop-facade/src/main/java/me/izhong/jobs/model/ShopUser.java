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
    private String phone;
    private String email;

}
