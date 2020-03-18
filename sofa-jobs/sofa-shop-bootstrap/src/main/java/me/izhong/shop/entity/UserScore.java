package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "USER_SCORE")
@SequenceGenerator(name = "USER_SCORE_SEQ", sequenceName = "USER_SCORE_SEQ", allocationSize = 1)
/**
 *  用户积分
 */
public class UserScore {
    @Id
    @GeneratedValue(generator = "USER_SCORE_SEQ", strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "AVA_SCORE")
    private Long availableScore;
    @Column(name = "UNAVA_Score")
    private Long unavailableScore;
    @Column(name = "USER_ID")
    private Long userId;
}
