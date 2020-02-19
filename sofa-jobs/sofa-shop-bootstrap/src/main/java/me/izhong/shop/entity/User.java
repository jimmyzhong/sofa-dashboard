package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

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
    @Column(name = "PASSWORD", nullable = false, length = 50)
    private String password;
    @Column(name = "USER_NAME", unique = true, nullable = false, length = 50)
    private String userName;
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;
    @Column(name = "EMAIL", length = 20)
    private String email;
    @Column(name = "IDENTITY", length = 20)
    private String identityID;
    @Column(name = "CERTIFIED", length = 1)
    private Boolean isCertified;
    @Column(name = "LOCKED", length = 1)
    private Boolean isLocked;

}
