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
import me.izhong.shop.dao.ArticlesDao;
import me.izhong.shop.entity.Articles;
import me.izhong.shop.service.IArticlesService;

@Service
public class ArticlesService implements IArticlesService {

	@Autowired
	private ArticlesDao articlesDao;

	@Override
	@Transactional
	public void saveOrUpdate(Articles articles) {
		articlesDao.save(articles);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		articlesDao.deleteById(id);
	}

	@Override
	public Articles findById(Long id) {
		return articlesDao.findById(id).orElseThrow(()-> BusinessException.build("找不到文章" + id));
	}

	@Override
	@Transactional(readOnly=true)
	public PageModel<Articles> pageList(PageRequest request, String title) {
		Articles article = new Articles();
		if (!StringUtils.isEmpty(title)) {
			article.setTitle(title);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains());

		Example<Articles> example = Example.of(article, matcher);
		Sort sort = Sort.unsorted();
		if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getIsAsc())) {
			sort = Sort.by("asc".equalsIgnoreCase(request.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					request.getOrderByColumn());
		}

		Pageable pageableReq = of(
				Long.valueOf(request.getPageNum()-1).intValue(),
				Long.valueOf(request.getPageSize()).intValue(), sort);
		Page<Articles> page = articlesDao.findAll(example, pageableReq);
		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

}
