package me.izhong.shop.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LotsDTO {
    @Tolerate
    public LotsDTO() {

    }
    private Long id;
    private String lotsNo; // 拍品编号
    private String name;
    private String description;
    private Long goodsId;
    private Integer lotCategoryId;
    private String content;
    //起拍价
    private BigDecimal startPrice;
    //成交下限,低于此价格不成交
    private BigDecimal lowerLimit;
    private BigDecimal higherLimit;
    //加价幅度
    private BigDecimal addPrice;
    //保证金
    private BigDecimal deposit;
    //拍卖开始时间
    private LocalDateTime startTime;
    //拍卖结束时间
    private LocalDateTime endTime;
    //用户参与拍卖的级别
    private Integer userLevel;
    //房间密码
    private String password;
    private Integer followCount;
    //销售价
    private BigDecimal salePrice;
    //现价
    private BigDecimal nowPrice;
    //拍卖状态
    private Integer payStatus;
    private String orderSn;
    private LocalDateTime paidAt;
    private String payMethod;
    private String payNo;
    private Integer isApply;
    //1是二次拍卖，2是平台转拍
    private Integer applyType;
    private Integer applyStatus;
    private Boolean over;  //是否成交
    //是否发布
    private Integer isRepublish;
    //警示价
    private BigDecimal warningPrice;
    //平台扣佣比例
    private Integer platformRatio;
    //平台佣金
    private BigDecimal platformAmount;
    //平台返佣
    private BigDecimal revenueAmount;
    //成交价
    private BigDecimal finalPrice;
    private Long finalUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer auctionFrequency; // 出价频率
    private Integer maxMemberCount;
    private String productPic;
    private List<String> albumPics;
    private Integer bidTimes;
    private String orderStatus;
    private String orderType;
}
