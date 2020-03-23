package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

import me.izhong.shop.entity.Order;
import me.izhong.shop.util.PageableConvertUtil;
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

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class ArticlesService implements IArticlesService {

	@Autowired
	private ArticlesDao articlesDao;

	@Override
	@Transactional
	public void saveOrUpdate(Articles articles) {
		if(articles.getId()==null){
			articlesDao.save(articles);
		}else{
			Articles ats = findById(articles.getId());
			ats.setTitle(articles.getTitle());
			ats.setContent(articles.getContent());
			ats.setUpdateTime(LocalDateTime.now());
			articlesDao.save(ats);
		}

	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		Articles ats = findById(id);
		if(StringUtils.isNotBlank(ats.getType())) {
			throw BusinessException.build("系统禁止删除文章" + ats.getTitle() + ats.getType());
		}
		articlesDao.deleteById(id);
	}

	@Override
	public Articles findById(Long id) {
		return articlesDao.findById(id).orElseThrow(()-> BusinessException.build("找不到文章" + id));
	}

	@Override
	public Articles findByType(String type) {
		return articlesDao.findByType(type);
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
		Page<Articles> page = articlesDao.findAll(example, PageableConvertUtil.toDataPageable(request));
		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

}
