package me.izhong.shop.service.mng;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.jobs.manage.IShopHomeMngFacade;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.dao.PayRecordDao;
import me.izhong.shop.dao.UserDao;

@Slf4j
@Service
@SofaService(interfaceType = IShopHomeMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopHomeMngFacadeImpl implements IShopHomeMngFacade {
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private PayRecordDao payRecordDao;

	@Override
	public long countUser() {
		LocalDateTime now = LocalDateTime.now();
		return userDao.countUserByRegisterTime(now);
	}

	@Override
	public long countNormalGoods() {
		return payRecordDao.countGoodsByType(MoneyTypeEnum.NORMAL_GOODS.getDescription());
	}

	@Override
	public long countConsignmentGoods() {
		return payRecordDao.countGoodsByType(MoneyTypeEnum.NORMAL_GOODS.getDescription());
	}
}
