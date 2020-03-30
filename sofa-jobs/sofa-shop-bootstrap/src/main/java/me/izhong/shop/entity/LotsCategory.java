package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "LOTS_CAT")
@SequenceGenerator(name = "LOTS_CAT_SEQ", sequenceName = "LOTS_CAT_SEQ", allocationSize = 1)
public class LotsCategory {

    @Id
    @GeneratedValue(generator = "LOTS_CAT_SEQ", strategy = GenerationType.SEQUENCE)
    private Integer id;
    @Column(name = "NAME", length = 50)
    private String name;
    @Column(name = "LOGO", length = 500)
    private String logo;
    @Column(name = "PASSWORD", length = 20)
    private String password;
    @Column(name = "ADMIN")
    private Long admin;
    @Column(name = "SORT")
    private Integer sort;
    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
