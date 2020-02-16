package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "PERMISSION")
@SequenceGenerator(name = "PERM_SEQ", sequenceName = "PERM_SEQ", allocationSize = 1)
public class Permission extends PersistedEntity {
    @Id
    @GeneratedValue(generator = "ROLE_SEQ", strategy = GenerationType.SEQUENCE)
    private Integer id;
    @Column(name="PERM_NAME", unique = true, nullable = false, length = 10)
    private String name;
    @Column(length = 20)
    private String description;
    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles;
}
