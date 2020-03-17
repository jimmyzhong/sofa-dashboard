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
import me.izhong.shop.dao.TemplateDao;
import me.izhong.shop.entity.Template;
import me.izhong.shop.service.ITemplateService;

@Service
public class TemplateService implements ITemplateService {

	@Autowired
	private TemplateDao templateDao;

	@Override
	@Transactional
	public void saveOrUpdate(Template template) {
		templateDao.save(template);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		templateDao.deleteById(id);
	}

	@Override
	public Template findById(Long id) {
		return templateDao.findById(id).orElseThrow(()-> BusinessException.build("找不到模板" + id));
	}

	@Override
	@Transactional(readOnly=true)
	public PageModel<Template> pageList(PageRequest request, String title) {
		Template template = new Template();
		if (!StringUtils.isEmpty(title)) {
			template.setTitle(title);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains());

		Example<Template> example = Example.of(template, matcher);
		Sort sort = Sort.unsorted();
		if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getOrderDirection())) {
			sort = Sort.by("asc".equalsIgnoreCase(request.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					request.getOrderByColumn());
		}

		Pageable pageableReq = of(
				Long.valueOf(request.getPageNum()-1).intValue(),
				Long.valueOf(request.getPageSize()).intValue(), sort);
		Page<Template> page = templateDao.findAll(example, pageableReq);
		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

}
