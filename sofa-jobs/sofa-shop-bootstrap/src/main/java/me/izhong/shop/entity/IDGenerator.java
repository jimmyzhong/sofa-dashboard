package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "ID_GENERATOR_TABLE")
@SequenceGenerator(name = "ID_GENERATOR_TABLE_SEQ", sequenceName = "ID_GENERATOR_TABLE_SEQ", allocationSize = 1)
public class IDGenerator {
    @Id
    @GeneratedValue(generator = "ID_GENERATOR_TABLE_SEQ", strategy = GenerationType.SEQUENCE)
    private Integer id;
    @Column(name = "TABLE_NAME")
    private String tableName;
    @Column(name = "VARIABLE")
    private String variable;
    @Column(name = "NEXT")
    private String next;
    @Column(name = "INCREMENT")
    private Long increment;
}
