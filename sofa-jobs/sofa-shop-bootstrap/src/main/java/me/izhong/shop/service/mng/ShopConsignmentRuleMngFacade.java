package me.izhong.shop.service.mng;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopConsignmentRuleMngFacade;
import me.izhong.jobs.model.ShopConsignmentRule;
import me.izhong.shop.dao.ConsignmentRuleDao;
import me.izhong.shop.entity.ConsignmentRule;
import me.izhong.shop.service.IConsignmentRuleService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopConsignmentRuleMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopConsignmentRuleMngFacade implements IShopConsignmentRuleMngFacade {

	@Autowired
	private ConsignmentRuleDao consignmentRuleDao;
	@Autowired
	private IConsignmentRuleService consignmentRuleService;

	@Override
	public void create(ShopConsignmentRule shopConsignmentRule) {
		ConsignmentRule rule = new ConsignmentRule();
		rule.setRuleNo(generateRuleNo());
		rule.setLimitRule(shopConsignmentRule.getLimitRule());
		rule.setCreateTime(LocalDateTime.now());
		rule.setUpdateTime(LocalDateTime.now());
		rule.setIsDelete(0);
		consignmentRuleService.saveOrUpdate(rule);
	}

	@Override
	public void edit(ShopConsignmentRule shopConsignmentRule) {
		ConsignmentRule rule = consignmentRuleService.findById(shopConsignmentRule.getId());
		rule.setBeginTime(shopConsignmentRule.getBeginTime());
		rule.setEndTime(shopConsignmentRule.getEndTime());
		rule.setLimitRule(shopConsignmentRule.getLimitRule());
		rule.setUpdateTime(LocalDateTime.now());
		consignmentRuleService.saveOrUpdate(rule);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				consignmentRuleService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopConsignmentRule> pageList(PageRequest request) {
		ConsignmentRule rule = new ConsignmentRule();

        Example<ConsignmentRule> example = Example.of(rule);
		Page<ConsignmentRule> page = consignmentRuleDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopConsignmentRule> list = page.getContent().stream().map(t -> {
        	ShopConsignmentRule obj = new ShopConsignmentRule();
            BeanUtils.copyProperties(t, obj);
        	getReduceValue(obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopConsignmentRule find(Long id) {
		ConsignmentRule rule = consignmentRuleService.findById(id);
    	ShopConsignmentRule obj = new ShopConsignmentRule();
    	BeanUtils.copyProperties(rule, obj);
    	getReduceValue(obj);
    	return obj;
	}

	private void getReduceValue(ShopConsignmentRule shopConsignmentRule) {
		JSONObject jsonObject = JSON.parseObject(shopConsignmentRule.getLimitRule());
		if (jsonObject != null) {
			shopConsignmentRule.setReduceValue(jsonObject.getString("ReduceValue"));
		}
	}

	private String generateRuleNo() {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
        String now = df.format(LocalDate.now());
        return now + getRandomNum(5);
	}

    private String getRandomNum(Integer num) {
        String base = "0123456789";
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
