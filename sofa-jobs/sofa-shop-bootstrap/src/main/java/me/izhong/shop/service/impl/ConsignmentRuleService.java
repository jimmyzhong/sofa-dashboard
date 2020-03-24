package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.ConsignmentRuleDao;
import me.izhong.shop.entity.ConsignmentRule;
import me.izhong.shop.service.IConsignmentRuleService;

@Service
public class ConsignmentRuleService implements IConsignmentRuleService {

	@Autowired
	private ConsignmentRuleDao consignmentRuleDao;

	@Override
	@Transactional
	public void saveOrUpdate(ConsignmentRule rule) {
		consignmentRuleDao.save(rule);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		consignmentRuleDao.deleteById(id);
	}

	@Override
	public ConsignmentRule findById(Long id) {
		return consignmentRuleDao.findById(id).orElseThrow(()-> BusinessException.build("找不到寄售规则" + id));
	}

	@Override
	public PageModel<ConsignmentRule> pageList(PageRequest request, String ruleNo) {
		ConsignmentRule rule = new ConsignmentRule();
		rule.setIsDelete(0);
		if (!StringUtils.isEmpty(ruleNo)) {
			rule.setRuleNo(ruleNo);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("ruleNo", ExampleMatcher.GenericPropertyMatchers.exact());

		Example<ConsignmentRule> example = Example.of(rule, matcher);
		Sort sort = Sort.unsorted();
		if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getOrderDirection())) {
			sort = Sort.by("asc".equalsIgnoreCase(request.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					request.getOrderByColumn());
		}

		Pageable pageableReq = of(
				Long.valueOf(request.getPageNum()-1).intValue(),
				Long.valueOf(request.getPageSize()).intValue(), sort);
		Page<ConsignmentRule> page = consignmentRuleDao.findAll(example, pageableReq);
		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

}
