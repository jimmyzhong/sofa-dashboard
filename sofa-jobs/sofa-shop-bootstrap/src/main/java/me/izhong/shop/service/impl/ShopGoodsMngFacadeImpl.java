package me.izhong.shop.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.manage.IShopGoodsMngFacade;
import me.izhong.jobs.model.ShopGoods;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.entity.Goods;

@Service
@SofaService(interfaceType = IShopGoodsMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
@Slf4j
public class ShopGoodsMngFacadeImpl implements IShopGoodsMngFacade {
	
	@Autowired
	private GoodsDao goodsDao;

	@Override
	public ShopGoods find(Long goodsId) {
		Goods goods = goodsDao.findById(goodsId).orElseThrow(()->BusinessException.build("unable to find user by " + goodsId));
		ShopGoods shopGoods = new ShopGoods();
        BeanUtils.copyProperties(goods, shopGoods);
        return shopGoods;
	}

	@Override
	public boolean disable(Long goodsId) {
		return false;
	}

	@Override
	public boolean enable(Long goodsId) {
		return false;
	}

	@Override
	public ShopGoods edit(ShopGoods goods) {
		return null;
	}

	@Override
	public PageModel<ShopGoods> pageList(PageRequest request, ShopGoods group) {
		return null;
	}

	@Override
	public boolean remove(Long jobId) {
		return false;
	}

}
