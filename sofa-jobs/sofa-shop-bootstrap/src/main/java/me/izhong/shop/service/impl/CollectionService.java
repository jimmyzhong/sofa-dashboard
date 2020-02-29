package me.izhong.shop.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import me.izhong.shop.dao.CollectionDao;
import me.izhong.shop.dto.GoodsCollectionParam;
import me.izhong.shop.entity.UserCollection;
import me.izhong.shop.service.ICollectionService;

@Service
public class CollectionService implements ICollectionService {

	@Autowired
	private CollectionDao collectionDao;

	@Override
	@Transactional
	public void add(GoodsCollectionParam param) {
		UserCollection collection = collectionDao.findByUserIdAndProductId(param.getUserId(), param.getProductId());
		if (collection == null) {
			UserCollection newCollection = new UserCollection();
			BeanUtils.copyProperties(param, newCollection);
			collectionDao.save(newCollection);
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
			return items.stream().map(item -> {
				GoodsCollectionParam param = new GoodsCollectionParam();
				BeanUtils.copyProperties(item, param);
				return param;
			}).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}
}
