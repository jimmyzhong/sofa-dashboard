package me.izhong.shop.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.consts.ProductTypeEnum;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.dao.GoodsStoreDao;
import me.izhong.shop.dao.OrderItemDao;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsStore;
import me.izhong.shop.entity.Order;
import me.izhong.shop.entity.OrderItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
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
                // TODO updateResaleGoodsPrice();
            }catch (Throwable throwable) {
                log.error("goods price update error", throwable);
            }
        }, 1, 60, TimeUnit.MINUTES);
    }

    @Transactional
    public void updateResaleGoodsPrice() {
        List<Goods> goods = goodsDao.findAllByProductTypeAndCreateTimeBeforeAndCreatedByIsNotNull(
                ProductTypeEnum.RESALE.getType(),
                LocalDateTime.now().minusDays(1));
        log.info("get resale created more than one day " + goods.size());
        List<Goods> decayList = new ArrayList<>();
        for (Goods g: goods) {
            BigDecimal price = g.getPrice();
            // TODO correct to be idempotent
            BigDecimal newPrice = price.multiply(BigDecimal.valueOf(1-decayFactor));
            BigDecimal limit = g.getOriginalPrice().multiply(BigDecimal.valueOf(decayLimit));
            if (newPrice.compareTo(limit) >0) {
                g.setPrice(newPrice);
                decayList.add(g);
            }
        }
        log.info("those to be decayed " + decayList.size());
        if (!decayList.isEmpty()) {
            goodsDao.saveAll(decayList);
        }
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
            goods.setOriginalPrice(goods.getPrice());
            goods.setStock(goodsOrderItemMap.get(g.getId()).getQuantity());
            goods.setProductType(ProductTypeEnum.RESALE.getType());
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
        }
        storeDao.saveAll(goodsStores);
        order.setStatus(OrderStateEnum.RESALED.getState());
        order.setUpdateTime(LocalDateTime.now());
        orderService.saveOrUpdate(order);
    }
}
