package me.izhong.shop.bid.rat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitVo {

    private String url;

    private boolean isLimit;

    private Double interval;

    private Integer maxPermits;

    private Integer initialPermits;

}