package me.izhong.shop.service;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.shop.entity.ConsignmentRule;

public interface IConsignmentRuleService {

    void saveOrUpdate(ConsignmentRule rule);

	void deleteById(Long id);

	ConsignmentRule findById(Long id);

    PageModel<ConsignmentRule> pageList(PageRequest request, String ruleNo);
}
