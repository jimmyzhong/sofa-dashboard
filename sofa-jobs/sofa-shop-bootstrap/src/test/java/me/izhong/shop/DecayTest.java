package me.izhong.shop;

import com.alibaba.fastjson.JSON;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.util.ShareCodeUtil;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static me.izhong.shop.util.ShareCodeUtil.decodeUserCode;
import static me.izhong.shop.util.ShareCodeUtil.generateUserCode;
import static org.junit.Assert.assertEquals;

public class DecayTest {

    @Test
    public void testFixedRateDecay() {
        LocalDateTime created = LocalDateTime.parse("2020-03-12 01:20:22", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Long hours = Duration.between(created, LocalDateTime.now()).toHours();
        System.out.println("hours:" + hours);
        Long e = hours / 24;

        System.out.println("e:" + e.intValue());

        Long leftHours = 24 - (hours % 24 );
        System.out.println("timeLeft:" + leftHours);

        BigDecimal newPrice = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(1-0.1).pow(e.intValue()));
        System.out.println("newPrice " + newPrice);
    }

    @Test
    public void testUserCode() {
        assertEquals(Long.valueOf(ShareCodeUtil.SHORT_BITS_LIMITS),
                decodeUserCode(generateUserCode(ShareCodeUtil.SHORT_BITS_LIMITS)));
        assertEquals(Long.valueOf(ShareCodeUtil.SHORT_BITS_LIMITS + 100),
                decodeUserCode(generateUserCode(ShareCodeUtil.SHORT_BITS_LIMITS + 100)));
        for (Long i=31l; i<126; i++) {
            String code = generateUserCode(i);
            System.out.println(code);
            assertEquals(i, decodeUserCode(code));
        }
    }

    @Test
    public void testPassword() {
        String regex = "^(?![0-9]+$)[0-9A-Za-z]{8,16}$";

        String value = "aaa";  // 长度不够
        System.out.println(value+ ":" + value.matches(regex));

        value = "1111aaaa1111aaaaa";  // 太长
        System.out.println(value+ ":" + value.matches(regex));

        value = "111111111"; // 纯数字
        System.out.println(value+ ":" + value.matches(regex));

        value = "aaaaaaaaa"; // 纯字母
        System.out.println(value+ ":" + value.matches(regex));

        value = "####@@@@#"; // 特殊字符
        System.out.println(value+ ":" + value.matches(regex));

        value = "1111aaaa";  // 数字字母组合
        System.out.println(value+ ":" + value.matches(regex));

        value = "aaaa1111"; // 数字字母组合
        System.out.println(value+ ":" + value.matches(regex));

        value = "aa1111aa";	// 数字字母组合
        System.out.println(value+ ":" + value.matches(regex));

        value = "11aaaa11";	// 数字字母组合
        System.out.println(value+ ":" + value.matches(regex));

        value = "aa11aa11"; // 数字字母组合
        System.out.println(value+ ":" + value.matches(regex));
    }

    @Test
    public void test2() {
        String select = "select au.* from lots au, tx_order o, user u " +
                "where o.user_id = u.id and au.id=o.auction_id and u.id = ?1 and ";

        String subSignup = " ( o.order_type = " + MoneyTypeEnum.AUCTION_MARGIN.getType()
                + "and o.status = " + OrderStateEnum.PAID.getState() + " ) ";

        String subDeal = " ( o.order_type = " + MoneyTypeEnum.AUCTION_REMAIN.getType() + " ) ";
        String subRefund = " ( o.order_type = " + MoneyTypeEnum.AUCTION_MARGIN.getType()
                + "and o.status = " + OrderStateEnum.AUCTION_MARGIN_REFUND.getState() + " ) ";
        String auctionOfUserSql = select + " (" +subSignup + subDeal + subRefund +") ORDER BY ?#{#pageable}";

        System.out.println(auctionOfUserSql);
    }
}
