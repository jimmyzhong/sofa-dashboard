package me.izhong.shop.service;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.shop.entity.Template;

public interface ITemplateService {

    void saveOrUpdate(Template template);

	void deleteById(Long id);

	Template findById(Long id);

    PageModel<Template> pageList(PageRequest request, String title);
}
