package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import me.izhong.shop.util.PasswordUtils;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "USER")
@SequenceGenerator(name = "USER_SEQ", sequenceName = "USER_SEQ", allocationSize = 1)
public class User extends EditableEntity {
    @Id
    @GeneratedValue(generator = "USER_SEQ", strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "PASSWORD", nullable = false, length = 64)
    private String password;
    @Column(name = "LOGIN_NAME", length = 50)
    private String loginName;
    @Column(name = "NAME", length = 50)
    private String name;
    @Column(name = "PHONE",  length = 20)
    private String phone;
    @Column(name = "EMAIL", length = 20)
    private String email;
    @Column(name = "IDENTITY", length = 20)
    private String identityID;
    @Column(name = "CERTIFIED", length = 1)
    private Boolean isCertified;
    @Column(name = "LOCKED", length = 1)
    private Boolean isLocked;
    @Column(name = "SALT", length = 32)
    private String salt;
    @Column(name = "AVATAR", length = 200)
    private String avatar;



    public void encryptUserPassword() {
        setSalt(PasswordUtils.generateSalt(8));
        setPassword(PasswordUtils.encrypt(getPassword(), getSalt()));
    }
}
