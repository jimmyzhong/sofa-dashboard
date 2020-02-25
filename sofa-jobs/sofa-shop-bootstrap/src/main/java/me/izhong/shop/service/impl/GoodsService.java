package me.izhong.shop.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.service.IGoodsService;

@Slf4j
@Service
public class GoodsService implements IGoodsService {

	@Autowired
	private GoodsDao goodsDao;

	@Override
	@Transactional
	public Goods saveOrUpdate(Goods goods) {
		return goodsDao.save(goods);
	}

	@Override
	public Goods findById(Long goodsId) {
		return goodsDao.findById(goodsId).orElseThrow(() -> new RuntimeException("unable to find goods by " + goodsId));
	}

	@Override
	public void updatePublishStatusById(Integer publishStatus, List<Long> ids) {
		goodsDao.updatePublishStatus(publishStatus, ids);
	}

	@Override
	public void updateRecommendStatusById(Integer recommendStatus, List<Long> ids) {
		goodsDao.updateRecommendStatus(recommendStatus, ids);
	}

	@Override
	public void updateIsDeleteById(Integer deleteStatus, List<Long> ids) {
		goodsDao.updateIsDelete(deleteStatus, ids);
	}

	@Override
	public void deleteById(Long goodsId) {
		goodsDao.deleteById(goodsId);
	}

	@Override
    public void checkGoodsName(Goods goods, String goodsName) {
        if (!StringUtils.isEmpty(goodsName) && !StringUtils.equals(goods.getName(), goodsName)) {
            if (goodsDao.findByName(goodsName) != null ) {
                throw BusinessException.build(goodsName  + "已经存在");
            }
        }
        goods.setName(goodsName);
    }
}
