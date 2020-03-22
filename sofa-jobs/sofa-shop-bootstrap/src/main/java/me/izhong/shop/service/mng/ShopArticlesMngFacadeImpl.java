package me.izhong.shop.service.mng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopArticlesMngFacade;
import me.izhong.jobs.model.ShopArticles;
import me.izhong.shop.dao.ArticlesDao;
import me.izhong.shop.entity.Articles;
import me.izhong.shop.service.IArticlesService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopArticlesMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopArticlesMngFacadeImpl implements IShopArticlesMngFacade {

	@Autowired
	private ArticlesDao articlesDao;
	@Autowired
	private IArticlesService articlesService;

	@Override
	public void create(ShopArticles shopArticles) {
		Articles articles = new Articles();
		BeanUtils.copyProperties(shopArticles, articles);
		articles.setCreateTime(LocalDateTime.now());
		articles.setUpdateTime(LocalDateTime.now());
		articlesService.saveOrUpdate(articles);
	}

	@Override
	public void edit(ShopArticles shopArticles) {
		Articles articles = articlesService.findById(shopArticles.getId());
		articles.setTitle(shopArticles.getTitle());
		articles.setUpdateTime(LocalDateTime.now());
		articlesService.saveOrUpdate(articles);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				articlesService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopArticles> pageList(PageRequest request, String title) {
		Articles articles = new Articles();
		if (!StringUtils.isEmpty(title)) {
			articles.setTitle(title);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains());
		Example<Articles> example = Example.of(articles, matcher);

        Page<Articles> page = articlesDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopArticles> list = page.getContent().stream().map(t -> {
        	ShopArticles obj = new ShopArticles();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopArticles find(Long id) {
		Articles ad = articlesService.findById(id);
		ShopArticles obj = new ShopArticles();
        BeanUtils.copyProperties(ad, obj);
        return obj;
	}
}
