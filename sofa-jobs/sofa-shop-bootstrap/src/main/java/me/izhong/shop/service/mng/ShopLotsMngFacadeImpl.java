package me.izhong.shop.service.mng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopLotsMngFacade;
import me.izhong.jobs.model.ShopLots;
import me.izhong.jobs.model.ShopUser;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.consts.OrderStateEnum;
import me.izhong.shop.dao.LotsDao;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.ILotsService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopLotsMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopLotsMngFacadeImpl implements IShopLotsMngFacade {
	
	@Autowired
	private LotsDao lotsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ILotsService lotsService;

	@Override
	public void create(ShopLots shopLots) {
		Lots lots = new Lots();
		BeanUtils.copyProperties(shopLots, lots);
		lots.setCreateTime(LocalDateTime.now());
		lots.setUpdateTime(LocalDateTime.now());
		lotsService.saveOrUpdate(lots);
	}

	@Override
	public void edit(ShopLots shopLots) {
		Lots lots = lotsService.findById(shopLots.getId());
		//如果结束了，不允许编辑
		LocalDateTime now = LocalDateTime.now();
		if(lots.getEndTime().isBefore(now)) {
			throw BusinessException.build("拍卖已经结束，不能修改");
		}
		if(lots.getStartTime().isBefore(now)) {
			throw BusinessException.build("拍卖已经开始，不能修改");
		}
		if(lots.getStartTime().plusMinutes(1).isBefore(now)) {
			throw BusinessException.build("拍卖即将开始，不能修改");
		}
		lots.setName(shopLots.getName());
		lots.setDescription(shopLots.getDescription());
		lots.setGoodsId(shopLots.getGoodsId());
		lots.setLotCategoryId(shopLots.getLotCategoryId());
		lots.setStartPrice(shopLots.getStartPrice());
		lots.setAddPrice(shopLots.getAddPrice());
		lots.setDeposit(shopLots.getDeposit());
		lots.setStartTime(shopLots.getStartTime());
		lots.setEndTime(shopLots.getEndTime());
		lots.setUserLevel(shopLots.getUserLevel());
		lots.setSalePrice(shopLots.getSalePrice());
		lots.setNowPrice(shopLots.getNowPrice());
		lots.setIsRepublish(1);
		lots.setPlatformRatio(shopLots.getPlatformRatio());
		lots.setPlatformAmount(shopLots.getPlatformAmount());
		lots.setRevenueAmount(shopLots.getRevenueAmount());
		lots.setFinalPrice(shopLots.getFinalPrice());
		lots.setMaxMemberCount(shopLots.getMaxMemberCount());
		lots.setUpdateTime(LocalDateTime.now());
		lotsService.saveOrUpdate(lots);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				lotsService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopLots> pageList(PageRequest request, ShopLots search) {
		Lots lots = new Lots();
		if(search != null) {
			BeanUtils.copyProperties(search, lots);
		}
        Example<Lots> example = Example.of(lots);
		Page<Lots> page = lotsDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopLots> list = page.getContent().stream().map(t -> {
        	ShopLots obj = new ShopLots();
            BeanUtils.copyProperties( t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public List<ShopUser> auctionUserPageList(PageRequest request, Long auctionId) {
		List<User> userList = userDao.selectAcutionUsers(MoneyTypeEnum.AUCTION_MARGIN.getType(), auctionId, OrderStateEnum.PAID.getState());
		if (!CollectionUtils.isEmpty(userList)) {
			return userList.stream().map(t -> {
	            ShopUser shopUser = new ShopUser();
	            BeanUtils.copyProperties(t, shopUser);
	            return shopUser;
	        }).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}

	@Override
	public ShopLots find(Long id) {
		Lots lots = lotsService.findById(id);
		ShopLots shopLots = new ShopLots();
        BeanUtils.copyProperties(lots, shopLots);
        return shopLots;
	}
}
