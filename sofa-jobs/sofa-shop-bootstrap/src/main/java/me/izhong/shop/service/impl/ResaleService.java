package me.izhong.shop.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.consts.ProductTypeEnum;
import me.izhong.shop.dao.ConsignmentRuleDao;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.dao.GoodsStoreDao;
import me.izhong.shop.dao.OrderItemDao;
import me.izhong.shop.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResaleService {

    @Autowired
    GoodsDao goodsDao;
    @Autowired
    GoodsStoreDao storeDao;
    @Autowired
    OrderItemDao itemDao;
    @Autowired
    OrderService orderService;
    @Autowired
    ConsignmentRuleDao consignmentRuleDao;

    @Value("${order.resale.decay.period.hours}")
    private Integer decayPeriodHours;
    @Value("${order.resale.decay.factor}")
    private Double decayFactor;
    @Value("${order.resale.decay.limit}")
    private Double decayLimit;

    @PostConstruct
    public void setUp() {
        ScheduledExecutorService resaleGoodsPriceUpdater = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("resale-price-updater").build());
        resaleGoodsPriceUpdater.scheduleAtFixedRate(() -> {
            try {
                updateResaleGoodsPrice();
            }catch (Throwable throwable) {
                log.error("goods price update error", throwable);
            }
        }, 1, 60, TimeUnit.MINUTES);
    }

    @Transactional
    public void updateResaleGoodsPrice() {
        LocalDateTime now = LocalDateTime.now();
        List<Goods> goods = goodsDao.findAllByProductTypeAndCreateTimeBeforeAndCreatedByIsNotNullAndStockGreaterThan(
                ProductTypeEnum.RESALE.getType(),
                now.minusHours(decayPeriodHours), 0);
        log.info("get resale created more than one day " + goods.size());
        List<Goods> decayList = new ArrayList<>();

        ResaleConfig resaleConfig = new ResaleConfig().invoke();
        Double defaultTimeStep = resaleConfig.getTimeStep();
        Double defaultReduceValue = resaleConfig.getReduceValue();
        Double defaultReduceLimit = resaleConfig.getReduceLimit();

        for (Goods g: goods) {
            Double reduceLimit = defaultReduceLimit;
            Double reduceValue = defaultReduceValue;
            Double timeStep = defaultTimeStep;

            if (g.getResaleLimit() != null) {
                reduceLimit = g.getResaleLimit();
            }
            if (g.getResaleReduceValue() != null) {
                reduceValue = g.getResaleReduceValue();
            }
            if (g.getResaleTimeStep() != null) {
                timeStep = g.getResaleTimeStep();
            }

            log.info("check adjust price of goods  " + g.getId() + "," +reduceLimit +"," + reduceValue + ","+ timeStep);

            BigDecimal price = g.getPrice();
            BigDecimal limit = g.getOriginalPrice().multiply(BigDecimal.valueOf(reduceLimit))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            if (limit.compareTo(BigDecimal.ZERO) == 0) {
                limit = g.getOriginalPrice();
            }
            if (price.compareTo(limit) <= 0) {
                continue;
            }
            Long hours = Duration.between(g.getCreateTime(), now).toHours();
            Long e = hours / timeStep.longValue();
            BigDecimal newPrice = g.getOriginalPrice().multiply(BigDecimal.valueOf(1-reduceValue).pow(e.intValue()))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            if (newPrice.compareTo(limit) >=0) {
                g.setPrice(newPrice);
            } else {
                g.setPrice(limit);
            }
            // 价格没有变化
            if (price.compareTo(g.getPrice()) == 0) {
                continue;
            }
            decayList.add(g);
            goodsDao.updateProductPrice(g.getId(), g.getPrice());
        }
        log.info("those to be decayed " + decayList.size());
//        if (!decayList.isEmpty()) {
//            goodsDao.saveAll(decayList);
//        }
    }

    public LocalDateTime nextPriceTime(LocalDateTime createdTime) {
        LocalDateTime now = LocalDateTime.now();
        Long seconds = Duration.between(createdTime, now).getSeconds();
        Long leftSeconds = (decayPeriodHours * 3600) - (seconds % (decayPeriodHours * 3600));
        LocalDateTime priceTime = now.plusSeconds(leftSeconds);
        return priceTime;
    }

    @Transactional
    public void resaleOrder(Long userId, String orderNo) {
        Order order = orderService.findByOrderNo(orderNo);
        if (order == null ||  OrderStateEnum.PAID.getState() != order.getStatus()) {
            throw BusinessException.build("无法寄售该订单. 状态=" + OrderStateEnum.getCommentByState(order.getStatus()));
        }
        List<OrderItem> items = itemDao.findAllByOrOrderIdAndUserId(order.getId(), userId);
        Map<Long, OrderItem> goodsOrderItemMap = items.stream()
                .collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));
        List<Goods> existingGoods = goodsDao.findAllById(items.stream()
                .map(OrderItem::getProductId).collect(Collectors.toSet()));

        ResaleConfig resaleConfig = new ResaleConfig().invoke();
        Double timeStep = resaleConfig.getTimeStep();
        Double reduceValue = resaleConfig.getReduceValue();
        Double reduceLimit = resaleConfig.getReduceLimit();
        // create new goods
        List<Goods> newGoods = new ArrayList<>();
        for (Goods g : existingGoods) {
            Goods goods = new Goods();
            BeanUtils.copyProperties(g, goods);
            goods.setCreatedBy(userId);
            goods.setCreateTime(LocalDateTime.now());
            goods.setId(null);
            goods.setSale(0);
            goods.setIsDelete(0);
            goods.setProductSn(null);
            goods.setOriginalPrice(g.getPromotionPrice()==null ? g.getPrice() : g.getPromotionPrice());
            goods.setPrice(goods.getOriginalPrice());
            goods.setPromotionPrice(null);
            goods.setStock(goodsOrderItemMap.get(g.getId()).getQuantity());
            goods.setProductType(ProductTypeEnum.RESALE.getType());
            goods.setScoreRedeem(0);
            goods.setResaleReduceValue(reduceValue);
            goods.setResaleLimit(reduceLimit);
            goods.setResaleTimeStep(timeStep);
            newGoods.add(goods);
        }
        newGoods = goodsDao.saveAll(newGoods);

        // create new stock
        List<GoodsStore> goodsStores = new ArrayList<>();
        for (Goods g: newGoods) {
            GoodsStore gs = new GoodsStore();
            gs.setProductId(g.getId());
            gs.setPreStore(g.getStock());
            gs.setStore(g.getStock());
            goodsStores.add(gs);
        }
        storeDao.saveAll(goodsStores);
        order.setStatus(OrderStateEnum.RESALED.getState());
        order.setUpdateTime(LocalDateTime.now());
        orderService.saveOrUpdate(order);
    }

    private class ResaleConfig {
        private Double timeStep;
        private Double reduceValue;
        private Double reduceLimit;

        public Double getTimeStep() {
            return timeStep;
        }

        public Double getReduceValue() {
            return reduceValue;
        }

        public Double getReduceLimit() {
            return reduceLimit;
        }

        /**
         * 商品配置 > 系统配置 > 应用默认配置
         * @return
         */
        public ResaleConfig invoke() {
            ConsignmentRule rule = consignmentRuleDao.findFirstByIsDeleteIsNullOrIsDeleteOrderByCreateTime(0);
            timeStep = Double.valueOf(ResaleService.this.decayPeriodHours);
            reduceValue = ResaleService.this.decayFactor;
            reduceLimit = ResaleService.this.decayLimit;
            if (rule != null) {
                timeStep = Double.valueOf(rule.getTimeStep());
                reduceValue = Double.valueOf(rule.getReduceValue());
                if(reduceValue > 1) {
                    reduceValue = reduceValue / 100.0;
                }
            }
            return this;
        }
    }
}
