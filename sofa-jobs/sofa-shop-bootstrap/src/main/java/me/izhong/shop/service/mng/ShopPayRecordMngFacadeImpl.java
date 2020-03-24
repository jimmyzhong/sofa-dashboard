package me.izhong.shop.service.mng;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.manage.IShopPayRecordMngFacade;
import me.izhong.jobs.model.ShopPayRecord;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.entity.PayRecord;
import me.izhong.shop.service.impl.PayRecordService;

@Slf4j
@Service
@SofaService(interfaceType = IShopPayRecordMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopPayRecordMngFacadeImpl implements IShopPayRecordMngFacade {

	@Autowired
	private PayRecordService payRecordService;

	@Override
	public PageModel<ShopPayRecord> pageList(PageRequest request, Long userId, List<Integer> moneyTypes) {
        Set<MoneyTypeEnum> types = new HashSet<>();
        if (!CollectionUtils.isEmpty(moneyTypes)) {
            types = moneyTypes.stream().map(t -> {
                MoneyTypeEnum type = MoneyTypeEnum.getByType(t);
                if (type == null) {
                    throw BusinessException.build("类型信息不正确");
                }
                return type;
            }).collect(Collectors.toSet());
        }
		PageModel<PayRecord> page = payRecordService.listMoneyReturnRecord(userId, request, types);
		List<ShopPayRecord> list = page.getRows().stream().map(t -> {
			ShopPayRecord obj = new ShopPayRecord();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
		return PageModel.instance(page.getCount(), list);
	}

}
