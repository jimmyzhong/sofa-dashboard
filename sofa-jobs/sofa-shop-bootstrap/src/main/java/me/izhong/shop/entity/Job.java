package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "APP_JOB")
@SequenceGenerator(name = "APP_JOB_SEQ", sequenceName = "APP_JOB_SEQ", allocationSize = 1)
public class Job {
    @Id
    @GeneratedValue(generator = "APP_JOB_SEQ", strategy = GenerationType.SEQUENCE)
    private Integer id;
    @Column(name = "NAME", unique = true)
    private String name;
    @Column(name = "LAST_STATE")
    private Integer lastRunState;
    @Column(name = "LAST_TIME")
    private LocalDateTime lastRunTime;
}
