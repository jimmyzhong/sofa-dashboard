package me.izhong.shop.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import me.izhong.shop.dao.CollectionDao;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.dto.GoodsCollectionParam;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.UserCollection;
import me.izhong.shop.service.ICollectionService;

@Service
public class CollectionService implements ICollectionService {

	@Autowired
	private CollectionDao collectionDao;
	@Autowired
	private GoodsDao goodsDao;

	@Override
	@Transactional
	public void add(Long userId, Long productId) {
		UserCollection collection = collectionDao.findByUserIdAndProductId(userId, productId);
		if (collection == null) {
			collection = new UserCollection();
			collection.setUserId(userId);
			collection.setProductId(productId);
			collection.setCreateTime(LocalDateTime.now());
			collection.setUpdateTime(LocalDateTime.now());
			collectionDao.save(collection);
		}
	}

	@Override
	@Transactional
	public void delete(Long userId, Long productId) {
		collectionDao.deleteByUserIdAndProductId(userId, productId);
	}

	@Override
	public List<GoodsCollectionParam> list(Long userId) {
		List<UserCollection> items = collectionDao.findByUserId(userId);
		if (items != null) {
			return items.stream().map(t -> {
				GoodsCollectionParam param = new GoodsCollectionParam();
				BeanUtils.copyProperties(t, param);
				Optional<Goods> goodsOptional = goodsDao.findById(t.getProductId());
				if (goodsOptional.isPresent()) {
					Goods goods = goodsOptional.get();
					param.setProductName(goods.getProductName());
					param.setProductPic(goods.getProductPic());
					param.setProductPrice(goods.getPromotionPrice().toString());
				}
				return param;
			}).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}
}
