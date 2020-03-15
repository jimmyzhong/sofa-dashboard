package me.izhong.shop.service;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.shop.entity.Articles;

public interface IArticlesService {

    void saveOrUpdate(Articles articles);

	void deleteById(Long id);

	Articles findById(Long id);

    PageModel<Articles> pageList(PageRequest request, String title);
}
