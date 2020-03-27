package me.izhong.jobs.model.bid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class BidItems implements Serializable {


    /**
     * 出价序号
     */
    private Long seqId;

    /**
     * 出价用户ID
     */
    private Long userId;

    /**
     * 出价价格，单位分
     */
    private Long price;

    /**
     * 出价时间
     */
    private Date bidTime;

}
