package me.izhong.jobs.model.bid;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 报价信息，1分钟做一个定时器，最后一分钟报价参数不允许修改，但是允许参与人报名
 * 最后10s不允许参与人报名
 * 报价开始剩余5s的时候上送报价信息，包含报价价格修改，时间修改，参与人修改，接口禁止
 */
@Getter
@Setter
public class BidUploadInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报价开始价格，单位分
     */
    private Long startPrice;

    /**
     * 最大价格，单位分
     */
    private Long endPrice;

    /**
     * 每次加价金额，单位分
     */
    private Long stepPrice;

    /**
     * 最后一次报价是否可以超过 endPrice，默认给 true
     */
    private Boolean overPrice;

    /**
     * 报价开始时间
     */
    private Date startTime;

    /**
     * 报价结束时间
     */
    private Date endTime;

    /**
     * 能够有权限参与报价的用户信息
     */
    private List<BidUserInfo> users;

}
