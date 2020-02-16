package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name="ROLE")
@SequenceGenerator(name = "ROLE_SEQ", sequenceName = "ROLE_SEQ", allocationSize = 1)
public class Role extends PersistedEntity {
    @Id
    @GeneratedValue(generator = "ROLE_SEQ", strategy = GenerationType.SEQUENCE)
    private Integer id;
    @Column(name="ROLE_NAME", unique = true, nullable = false, length = 10)
    private String name;

    @ManyToMany
    @JoinTable(name="ROLE_PERMISSION",
            joinColumns = {@JoinColumn(name="ROLE_ID", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name="PERM_ID", nullable = false)})
    private Set<Permission> permissions;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}
